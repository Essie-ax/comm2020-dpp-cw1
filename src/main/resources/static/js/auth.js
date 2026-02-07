const $ = (id) => document.getElementById(id);

async function login() {
  $("err").textContent = "";

  const username = $("username").value.trim();
  const password = $("password").value;

  if (!username || !password) {
    $("err").textContent = "Please enter username and password.";
    return;
  }

  try {
    const res = await fetch("/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password })
    });

    const json = await res.json().catch(() => null);

    if (!json) {
      $("err").textContent = `Invalid response (status ${res.status})`;
      return;
    }

    if (!res.ok || json.success !== true) {
      $("err").textContent = json.error?.message || `Login failed (${res.status})`;
      return;
    }

    const { token, role, userId } = json.data;
    localStorage.setItem("token", token);
    localStorage.setItem("role", role);
    localStorage.setItem("userId", String(userId));

    
    if (role === "GAMEKEEPER") window.location.href = "/gamekeeper.html";
    else window.location.href = "/player.html";

  } catch (e) {
    $("err").textContent = "Network error (backend not running?)";
  }
}

document.getElementById("btn").addEventListener("click", login);
document.getElementById("password").addEventListener("keydown", (e) => {
  if (e.key === "Enter") login();
});
