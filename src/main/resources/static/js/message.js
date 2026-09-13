const form = document.querySelector("[data-message-form]");
const dialog = document.querySelector("[data-message-dialog]");

function openMessageDialog(body, displayName) {
  if (!dialog) {
    return;
  }
  dialog.querySelector("[data-dialog-body]").textContent = body;
  dialog.querySelector("[data-dialog-meta]").textContent = displayName || "匿名";
  dialog.showModal();
}

function bindPetal(petal) {
  petal.addEventListener("click", () => {
    openMessageDialog(petal.dataset.body, petal.dataset.displayName);
  });
}

document.querySelectorAll(".petal").forEach(bindPetal);

if (dialog) {
  dialog.querySelector("[data-dialog-close]").addEventListener("click", () => {
    dialog.close();
  });
}

if (form) {
  const textarea = form.querySelector("textarea[name='body']");
  const nameInput = form.querySelector("input[name='displayName']");
  const anonymousInput = form.querySelector("input[name='anonymous']");
  const termsInput = form.querySelector("input[name='termsAgreed']");
  const submitButton = form.querySelector("button[type='submit']");
  const counter = form.querySelector("[data-count]");
  const message = form.querySelector("[data-form-message]");
  const board = document.querySelector("[data-uka-board]");
  const count = document.querySelector("[data-message-count]");
  const list = document.querySelector("[data-message-list]");
  const csrfToken = document.querySelector("meta[name='_csrf']")?.content;
  const csrfHeader = document.querySelector("meta[name='_csrf_header']")?.content;

  const updateCount = () => {
    if (counter) {
      counter.textContent = textarea.value.length;
    }
  };

  textarea.addEventListener("input", updateCount);

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    const body = textarea.value.trim();
    if (!body) {
      message.textContent = "メッセージを入力してください。";
      return;
    }
    if (body.length > 300) {
      message.textContent = "メッセージは300文字以内で入力してください。";
      return;
    }
    if (nameInput.value.trim().length > 30) {
      message.textContent = "表示名は30文字以内で入力してください。";
      return;
    }
    if (termsInput && !termsInput.checked) {
      message.textContent = "投稿内容の確認にチェックを入れてください。";
      return;
    }

    message.textContent = "投稿中です...";
    if (submitButton) {
      submitButton.disabled = true;
    }
    const payload = {
      eventId: form.dataset.eventId,
      creatorId: form.dataset.creatorId,
      artworkId: form.dataset.artworkId || null,
      body,
      displayName: nameInput.value.trim(),
      anonymous: anonymousInput ? anonymousInput.checked : true,
      termsAgreed: termsInput ? termsInput.checked : true,
    };
    const headers = { "Content-Type": "application/json" };
    if (csrfToken && csrfHeader) {
      headers[csrfHeader] = csrfToken;
    }

    try {
      const response = await fetch("/api/messages", {
        method: "POST",
        headers,
        body: JSON.stringify(payload),
      });
      const result = await response.json();
      if (!response.ok) {
        throw new Error(result.message || "投稿に失敗しました。");
      }

      textarea.value = "";
      nameInput.value = "";
      updateCount();
      message.textContent = result.status === "PENDING"
          ? "内容確認後に公開されます。"
          : "あなたの言葉が、一枚の花びらになりました。";
      if (result.status === "PUBLISHED") {
        appendPetal(result, board, count);
        appendMessage(result, list);
      }
    } catch (error) {
      message.textContent = `${error.message} 再送できます。`;
    } finally {
      if (submitButton) {
        submitButton.disabled = false;
      }
    }
  });
}

function appendPetal(result, board, count) {
  if (!board) {
    return;
  }
  const petal = document.createElement("button");
  petal.type = "button";
  petal.className = "petal";
  petal.dataset.body = result.body;
  petal.dataset.displayName = result.displayName;
  petal.style.setProperty("--x", `${result.petalX}%`);
  petal.style.setProperty("--y", `${result.petalY}%`);
  petal.style.setProperty("--angle", `${result.petalAngle}deg`);
  petal.style.setProperty("--scale", result.petalSize);
  const span = document.createElement("span");
  span.textContent = result.body;
  petal.append(span);
  bindPetal(petal);
  board.append(petal);
  if (count) {
    count.textContent = Number(count.textContent || "0") + 1;
  }
}

function appendMessage(result, list) {
  if (!list) {
    return;
  }
  const article = document.createElement("article");
  const body = document.createElement("p");
  const name = document.createElement("small");
  body.textContent = result.body;
  name.textContent = result.displayName;
  article.append(body, name);
  list.prepend(article);
}
