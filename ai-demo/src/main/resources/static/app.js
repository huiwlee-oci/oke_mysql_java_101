const chatLog = document.querySelector("#chatLog");
const chatForm = document.querySelector("#chatForm");
const input = document.querySelector("#messageInput");
const sendButton = document.querySelector("#sendButton");
const clearButton = document.querySelector("#clearButton");
const statusText = document.querySelector("#statusText");

let messages = [];

addMessage("assistant", "Start a conversation with OCI Generative AI.");

chatForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  const content = input.value.trim();
  if (!content) {
    return;
  }

  input.value = "";
  resizeInput();
  messages.push({ role: "user", content });
  addMessage("user", content);
  await sendChat();
});

clearButton.addEventListener("click", () => {
  messages = [];
  chatLog.innerHTML = "";
  addMessage("assistant", "New conversation ready.");
  statusText.textContent = "Ready";
  input.focus();
});

input.addEventListener("input", resizeInput);
input.addEventListener("keydown", (event) => {
  if (event.key === "Enter" && !event.shiftKey) {
    event.preventDefault();
    chatForm.requestSubmit();
  }
});

async function sendChat() {
  setBusy(true);
  const typing = addMessage("assistant", "Thinking...", "typing");

  try {
    const response = await fetch("/api/chat", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ messages })
    });
    const payload = await response.json();
    typing.remove();

    if (!response.ok || payload.error) {
      addMessage("system", payload.error || "Request failed.");
      statusText.textContent = "Needs attention";
      return;
    }

    messages.push({ role: "assistant", content: payload.reply });
    addMessage("assistant", payload.reply);
    statusText.textContent = payload.opcRequestId ? `Request ${payload.opcRequestId}` : "Ready";
  } catch (error) {
    typing.remove();
    addMessage("system", "The chat service is unreachable.");
    statusText.textContent = "Offline";
  } finally {
    setBusy(false);
  }
}

function addMessage(role, content, extraClass = "") {
  const item = document.createElement("article");
  item.className = `message ${role} ${extraClass}`.trim();

  const label = document.createElement("div");
  label.className = "message-label";
  label.textContent = role === "user" ? "You" : role === "system" ? "System" : "Assistant";

  const bubble = document.createElement("div");
  bubble.className = "bubble";
  bubble.textContent = content;

  item.append(label, bubble);
  chatLog.append(item);
  chatLog.scrollTop = chatLog.scrollHeight;
  return item;
}

function setBusy(isBusy) {
  sendButton.disabled = isBusy;
  input.disabled = isBusy;
  statusText.textContent = isBusy ? "Thinking" : statusText.textContent;
  if (!isBusy) {
    input.focus();
  }
}

function resizeInput() {
  input.style.height = "auto";
  input.style.height = `${Math.min(input.scrollHeight, 180)}px`;
}
