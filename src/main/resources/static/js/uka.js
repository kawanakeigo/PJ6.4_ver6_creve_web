const ukaDialog = document.querySelector("[data-message-dialog]");

function showUkaMessage(body, displayName) {
  if (!ukaDialog) {
    return;
  }
  ukaDialog.querySelector("[data-dialog-body]").textContent = body;
  ukaDialog.querySelector("[data-dialog-meta]").textContent = displayName || "匿名";
  ukaDialog.showModal();
}

document.querySelectorAll(".petal").forEach((petal) => {
  petal.addEventListener("click", () => {
    showUkaMessage(petal.dataset.body, petal.dataset.displayName);
  });
});

if (ukaDialog) {
  ukaDialog.querySelector("[data-dialog-close]").addEventListener("click", () => {
    ukaDialog.close();
  });
}
