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

const openForm = document.getElementById("open-form");
const accessCodeInput = document.getElementById("access-code");
const openButton = document.getElementById("open-button");
const openMessage = document.getElementById("open-message");

const codePattern = /^[0-9]{8}$/;
const maxPdfSize = 10 * 1024 * 1024;
let sendMode = "pdf";

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

function resetIssuedCode() {
    showMessage(sendMessage, "");
    showMessage(copyMessage, "");
    issuedCode.hidden = true;
    issuedValue.value = "";
    previewLink.removeAttribute("href");
}

function updateSendFields() {
    const isPdf = sendMode === "pdf";
    const busy = sendButton.disabled;

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
    if (sendButton.disabled || sendMode === mode) {
        return;
    }
    sendMode = mode;
    originalUrlInput.removeAttribute("aria-invalid");
    pdfFileInput.removeAttribute("aria-invalid");
    resetIssuedCode();
    updateSendFields();
}

pdfModeButton.addEventListener("click", () => chooseSendMode("pdf"));
linkModeButton.addEventListener("click", () => chooseSendMode("link"));

sendForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    if (sendButton.disabled) {
        return;
    }

    const isPdf = sendMode === "pdf";
    resetIssuedCode();

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
    resetIssuedCode();
});

pdfFileInput.addEventListener("change", () => {
    pdfFileInput.removeAttribute("aria-invalid");
    resetIssuedCode();
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

accessCodeInput.addEventListener("input", () => {
    accessCodeInput.removeAttribute("aria-invalid");
    showMessage(openMessage, "");
});

openForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    if (openButton.disabled) {
        return;
    }

    const code = accessCodeInput.value.trim();
    showMessage(openMessage, "");

    if (!codePattern.test(code)) {
        accessCodeInput.setAttribute("aria-invalid", "true");
        showMessage(openMessage, "숫자 8자리 코드를 입력해 주세요.", "error");
        accessCodeInput.focus();
        return;
    }

    accessCodeInput.removeAttribute("aria-invalid");
    setBusy(openForm, openButton, true, "코드 확인 중…");
    accessCodeInput.readOnly = true;

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

        accessCodeInput.value = "";
        window.location.assign("/" + code);
    } catch {
        showMessage(openMessage, "자료를 열 수 없어요. 연결을 확인하고 다시 시도해 주세요.", "error");
    } finally {
        accessCodeInput.readOnly = false;
        setBusy(openForm, openButton, false, "자료 열기 →");
    }
});

updateSendFields();
