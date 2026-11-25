const sendForm = document.getElementById("send-form");
const originalUrlInput = document.getElementById("original-url");
const pdfFileInput = document.getElementById("pdf-file");
const pdfModeButton = document.getElementById("pdf-mode");
const linkModeButton = document.getElementById("link-mode");
const pdfFields = document.getElementById("pdf-fields");
const linkFields = document.getElementById("link-fields");
const issuedNote = document.getElementById("issued-note");
const sendButton = document.getElementById("send-button");
const sendMessage = document.getElementById("send-message");
const issuedCode = document.getElementById("issued-code");
const issuedValue = document.getElementById("issued-value");
const copyButton = document.getElementById("copy-button");
const copyMessage = document.getElementById("copy-message");
const previewLink = document.getElementById("preview-link");
const cancelActions = document.getElementById("cancel-actions");
const cancelButton = document.getElementById("cancel-button");
const cancelMessage = document.getElementById("cancel-message");

const openForm = document.getElementById("open-form");
const accessCodeInput = document.getElementById("access-code");
const openButton = document.getElementById("open-button");
const openMessage = document.getElementById("open-message");
const openFallback = document.getElementById("open-fallback");

const codePattern = /^[0-9]{8}$/;
const maxPdfSize = 10 * 1024 * 1024;
let sendMode = "pdf";
let issuedPdf = null;
let cancelling = false;
const draftKey = "print-link-issued-pdf";
const fileRoutePattern = /^\/pdf\/pdf-[0-9]+\.pdf$/;
const tokenPattern = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

function showMessage(element, message, state = "") {
    element.textContent = message;
    element.className = "form-message" + (state ? " " + state : "");
    element.hidden = !message;
}

function setBusy(form, button, busy, label) {
    form.setAttribute("aria-busy", String(busy));
    button.disabled = busy;
    button.textContent = label;
}

function validOriginalUrl(value) {
    if (!/^https?:\/\//i.test(value)) {
        return false;
    }
    try {
        const url = new URL(value);
        return (url.protocol === "http:" || url.protocol === "https:") && Boolean(url.hostname);
    } catch {
        return false;
    }
}

function storeIssuedPdf(pdf) {
    issuedPdf = pdf;
    try {
        if (pdf) {
            sessionStorage.setItem(draftKey, JSON.stringify(pdf));
        } else {
            sessionStorage.removeItem(draftKey);
        }
    } catch {
        // 현재 화면의 취소 기능은 저장소 접근 제한 시에도 사용할 수 있다.
    }
}

function resetIssuedCode() {
    showMessage(sendMessage, "");
    showMessage(copyMessage, "");
    issuedCode.hidden = true;
    issuedValue.value = "";
    previewLink.removeAttribute("href");
    cancelActions.hidden = true;
    showMessage(cancelMessage, "");
    storeIssuedPdf(null);
}

function updateSendFields() {
    const isPdf = sendMode === "pdf";
    const busy = sendButton.disabled || cancelling;
    cancelButton.disabled = busy;
    copyButton.disabled = cancelling;

    pdfModeButton.setAttribute("aria-pressed", String(isPdf));
    linkModeButton.setAttribute("aria-pressed", String(!isPdf));
    pdfModeButton.disabled = busy;
    linkModeButton.disabled = busy;
    pdfFields.hidden = !isPdf;
    linkFields.hidden = isPdf;
    pdfFileInput.disabled = busy || !isPdf;
    originalUrlInput.disabled = busy || isPdf;
}

function chooseSendMode(mode) {
    if (sendButton.disabled || cancelling || sendMode === mode) {
        return;
    }
    sendMode = mode;
    originalUrlInput.removeAttribute("aria-invalid");
    pdfFileInput.removeAttribute("aria-invalid");
    showMessage(sendMessage, "");
    updateSendFields();
}

pdfModeButton.addEventListener("click", () => chooseSendMode("pdf"));
linkModeButton.addEventListener("click", () => chooseSendMode("link"));

sendForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    if (sendButton.disabled || cancelling) {
        return;
    }

    const isPdf = sendMode === "pdf";
    showMessage(sendMessage, "");
    if (issuedPdf && !window.confirm("새 코드를 발급하면 현재 PDF의 취소 버튼이 사라집니다. 계속할까요?")) {
        return;
    }

    let endpoint;
    let request;

    if (isPdf) {
        const file = pdfFileInput.files[0];

        if (!file || file.size === 0) {
            pdfFileInput.setAttribute("aria-invalid", "true");
            showMessage(sendMessage, "업로드할 PDF 파일을 선택해 주세요.", "error");
            pdfFileInput.focus();
            return;
        }

        if (file.size > maxPdfSize) {
            pdfFileInput.setAttribute("aria-invalid", "true");
            showMessage(sendMessage, "PDF는 최대 10MB까지 업로드할 수 있어요.", "error");
            pdfFileInput.focus();
            return;
        }

        if (!file.name.toLowerCase().endsWith(".pdf") && file.type !== "application/pdf") {
            pdfFileInput.setAttribute("aria-invalid", "true");
            showMessage(sendMessage, "PDF 파일을 선택해 주세요.", "error");
            pdfFileInput.focus();
            return;
        }

        pdfFileInput.removeAttribute("aria-invalid");
        const fileData = new FormData();
        fileData.append("file", file);
        endpoint = "/pdf";
        request = { method: "POST", body: fileData };
    } else {
        const originalUrl = originalUrlInput.value.trim();

        if (!validOriginalUrl(originalUrl)) {
            originalUrlInput.setAttribute("aria-invalid", "true");
            showMessage(sendMessage, "http:// 또는 https://로 시작하는 웹 링크를 입력해 주세요.", "error");
            originalUrlInput.focus();
            return;
        }

        const encodedLength = originalUrl.normalize("NFC").replace(/[^\x00-\x7F]+/gu, encodeURIComponent).length;
        if (originalUrl.length > 2048 || encodedLength > 2048) {
            originalUrlInput.setAttribute("aria-invalid", "true");
            showMessage(sendMessage, "링크가 너무 길어요. 한글 등은 인코딩한 뒤 최대 2,048자까지 보낼 수 있어요.", "error");
            originalUrlInput.focus();
            return;
        }

        originalUrlInput.removeAttribute("aria-invalid");
        endpoint = "/shortenUrl";
        request = {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ originalUrl })
        };
    }

    setBusy(sendForm, sendButton, true, isPdf ? "PDF 업로드 중…" : "코드 발급 중…");
    updateSendFields();

    try {
        const response = await fetch(endpoint, request);

        if (response.status === 413) {
            showMessage(sendMessage, "PDF는 최대 10MB까지 업로드할 수 있어요.", "error");
            return;
        }

        if (response.status === 400) {
            showMessage(sendMessage, isPdf ? "PDF 파일을 다시 확인해 주세요." : "링크 주소를 다시 확인해 주세요.", "error");
            return;
        }

        if (!response.ok) {
            throw new Error("코드 발급 실패");
        }

        const data = await response.json();

        if (typeof data.shortenUrlKey !== "string" || !codePattern.test(data.shortenUrlKey)) {
            throw new Error("잘못된 응답");
        }

        if (isPdf && (!fileRoutePattern.test(data.originalUrl) || !tokenPattern.test(data.cancelToken))) {
            throw new Error("PDF 취소 정보가 누락된 응답");
        }

        resetIssuedCode();
        if (isPdf) {
            storeIssuedPdf({
                code: data.shortenUrlKey,
                path: data.originalUrl,
                token: data.cancelToken
            });
            cancelActions.hidden = false;
        }
        issuedValue.value = data.shortenUrlKey;
        previewLink.href = "/" + data.shortenUrlKey;
        issuedNote.textContent = isPdf
            ? "공용 PC에서 이 코드를 입력하세요. PDF는 업로드 후 10분 동안 열 수 있어요."
            : "공용 PC에서 프린트 링크를 열고 이 코드를 입력하세요.";
        issuedCode.hidden = false;
        showMessage(sendMessage, "코드가 발급되었어요.", "success");
    } catch {
        showMessage(sendMessage, "코드를 발급할 수 없어요. 연결을 확인하고 다시 시도해 주세요.", "error");
    } finally {
        setBusy(sendForm, sendButton, false, "8자리 코드 발급");
        updateSendFields();
    }
});

originalUrlInput.addEventListener("input", () => {
    originalUrlInput.removeAttribute("aria-invalid");
    showMessage(sendMessage, "");
});

pdfFileInput.addEventListener("change", () => {
    pdfFileInput.removeAttribute("aria-invalid");
    showMessage(sendMessage, "");
});

copyButton.addEventListener("click", async () => {
    if (!issuedValue.value) {
        return;
    }

    if (navigator.clipboard && window.isSecureContext) {
        try {
            await navigator.clipboard.writeText(issuedValue.value);
            showMessage(copyMessage, "코드를 복사했어요.", "success");
            return;
        } catch {
            // HTTP 접속이나 클립보드 권한 제한이 있을 때 수동 복사를 제공한다.
        }
    }

    issuedValue.focus();
    issuedValue.select();
    issuedValue.setSelectionRange(0, issuedValue.value.length);

    try {
        if (document.execCommand("copy")) {
            showMessage(copyMessage, "코드를 복사했어요.", "success");
            return;
        }
    } catch {
        // 브라우저에서 복사를 지원하지 않으면 선택한 코드의 수동 복사를 안내한다.
    }

    showMessage(copyMessage, "선택된 코드를 복사해 주세요.");
});

cancelButton.addEventListener("click", async () => {
    if (!issuedPdf || cancelling || sendButton.disabled) {
        return;
    }
    if (!window.confirm("전송을 취소하면 PDF가 삭제되고 발급된 코드는 사용할 수 없어요. 삭제할까요?")) {
        return;
    }

    const pdf = issuedPdf;
    cancelling = true;
    sendButton.disabled = true;
    cancelButton.textContent = "삭제 중…";
    updateSendFields();
    showMessage(cancelMessage, "");

    try {
        const response = await fetch(pdf.path, {
            method: "DELETE",
            headers: { "X-Cancel-Token": pdf.token },
            cache: "no-store"
        });

        if (response.status === 404) {
            const availability = await fetch("/shortenUrl/" + pdf.code, { cache: "no-store" });
            if (availability.status !== 404) {
                throw new Error("취소할 수 없는 PDF");
            }
        } else if (response.status !== 204) {
            throw new Error("삭제 실패");
        }

        resetIssuedCode();
        pdfFileInput.value = "";
        showMessage(sendMessage, "전송이 취소되었어요. 이 코드는 더 이상 사용할 수 없어요.", "success");
    } catch {
        showMessage(cancelMessage, "자료를 삭제하지 못했어요. 잠시 후 다시 시도해 주세요.", "error");
    } finally {
        cancelling = false;
        sendButton.disabled = false;
        cancelButton.textContent = "전송 취소 · PDF 삭제";
        updateSendFields();
    }
});

async function restoreIssuedPdf() {
    try {
        const pdf = JSON.parse(sessionStorage.getItem(draftKey));
        if (!pdf || !codePattern.test(pdf.code)
                || !fileRoutePattern.test(pdf.path) || !tokenPattern.test(pdf.token)) {
            return;
        }
        const response = await fetch("/shortenUrl/" + pdf.code, { cache: "no-store" });
        if (response.status === 404) {
            storeIssuedPdf(null);
            return;
        }
        if (!response.ok || issuedPdf || sendButton.disabled) {
            return;
        }

        const data = await response.json();
        if (data.originalUrl !== pdf.path || data.shortenUrlKey !== pdf.code || issuedPdf || sendButton.disabled) {
            return;
        }
        issuedValue.value = pdf.code;
        previewLink.href = "/" + pdf.code;
        issuedNote.textContent = "공용 PC에서 이 코드를 입력하세요. PDF는 업로드 후 10분 동안 열 수 있어요.";
        issuedCode.hidden = false;
        cancelActions.hidden = false;
        issuedPdf = pdf;
    } catch {
        // 브라우저 저장소 또는 서버에 일시적으로 연결할 수 없는 경우 새 발급은 계속 가능하다.
    }
}

function hideOpenFallback() {
    openFallback.hidden = true;
    openFallback.removeAttribute("href");
}

accessCodeInput.addEventListener("input", () => {
    accessCodeInput.removeAttribute("aria-invalid");
    showMessage(openMessage, "");
    hideOpenFallback();
});

openForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    if (openButton.disabled) {
        return;
    }

    const code = accessCodeInput.value.trim();
    showMessage(openMessage, "");
    hideOpenFallback();

    if (!codePattern.test(code)) {
        accessCodeInput.setAttribute("aria-invalid", "true");
        showMessage(openMessage, "숫자 8자리 코드를 입력해 주세요.", "error");
        accessCodeInput.focus();
        return;
    }

    accessCodeInput.removeAttribute("aria-invalid");
    setBusy(openForm, openButton, true, "코드 확인 중…");
    accessCodeInput.readOnly = true;

    // 사용자 클릭 시점에 새 탭을 열어 비동기 조회 후 팝업 차단되는 상황을 줄인다.
    let destinationTab = null;
    let opened = false;

    try {
        destinationTab = window.open("about:blank", "_blank");
        if (destinationTab) {
            destinationTab.opener = null;
        }
    } catch {
        // 새 탭을 열 수 없으면 조회 성공 후 링크를 제공한다.
        if (destinationTab && !destinationTab.closed) {
            destinationTab.close();
        }
        destinationTab = null;
    }

    try {
        const response = await fetch("/shortenUrl/" + code, {
            cache: "no-store"
        });

        if (response.status === 404) {
            accessCodeInput.setAttribute("aria-invalid", "true");
            showMessage(openMessage, "존재하지 않거나 만료된 코드예요. 휴대폰에서 코드를 다시 발급받아 주세요.", "error");
            accessCodeInput.focus();
            return;
        }

        if (!response.ok) {
            throw new Error("코드 조회 실패");
        }

        const data = await response.json();
        if (data.shortenUrlKey !== code) {
            throw new Error("잘못된 응답");
        }

        const target = new URL("/" + code, window.location.href).href;
        if (destinationTab && !destinationTab.closed) {
            try {
                destinationTab.location.replace(target);
                opened = true;
            } catch {
                // 새 탭이 닫혔거나 이동을 허용하지 않으면 직접 열기 링크를 이용한다.
            }
        }

        accessCodeInput.value = "";
        if (opened) {
            showMessage(openMessage, "새 탭에서 자료를 열었어요.", "success");
        } else {
            openFallback.href = target;
            openFallback.hidden = false;
            showMessage(openMessage, "아래 버튼을 눌러 새 탭에서 자료를 열어 주세요.");
        }
    } catch {
        showMessage(openMessage, "자료를 열 수 없어요. 연결을 확인하고 다시 시도해 주세요.", "error");
    } finally {
        if (!opened && destinationTab && !destinationTab.closed) {
            destinationTab.close();
        }
        accessCodeInput.readOnly = false;
        setBusy(openForm, openButton, false, "자료 열기 →");
    }
});

updateSendFields();
restoreIssuedPdf();
