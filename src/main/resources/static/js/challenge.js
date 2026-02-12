const $ = (id) => document.getElementById(id);
const token = localStorage.getItem("token");
const role = localStorage.getItem("role");

// Load challenges when page is ready
function initChallengeSection() {
  $("info").textContent = "Logged in as: " + (localStorage.getItem("userId") || "?")
      + " | role: " + (role || "?");

  // If Player, redirect to player.html
  if (role === "PLAYER") {
    window.location.href = "/player.html";
    return;
  }

  // Hide create form if not GameKeeper
  if (role !== "GAME_KEEPER" && role !== "GAMEKEEPER") {
    $("createSection").style.display = "none";
  }

  loadChallenges();
}

// Fetch challenge list from backend
async function loadChallenges() {
  $("listMsg").textContent = "";
  try {
    const res = await fetch("/api/challenges");
    const json = await res.json();
    if (json.success && json.data && json.data.challenges) {
      renderChallengeList(json.data.challenges);
    } else {
      $("listMsg").className = "msg-err";
      $("listMsg").textContent = json.error?.message || "Failed to load challenges";
    }
  } catch (e) {
    $("listMsg").className = "msg-err";
    $("listMsg").textContent = "Network error: " + e.message;
  }
}

// Render challenge list rows
function renderChallengeList(challenges) {
  const tbody = $("challengeList");
  if (challenges.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:#999;">No challenges yet.</td></tr>';
    return;
  }
  tbody.innerHTML = challenges.map(c =>
    "<tr>" +
      "<td>" + c.challengeId + "</td>" +
      "<td>" + esc(c.title) + "</td>" +
      "<td>" + esc(c.category) + "</td>" +
      "<td>" + esc(c.startDate) + "</td>" +
      "<td>" + esc(c.endDate) + "</td>" +
      '<td><button class="btn-small" onclick="loadChallengeDetail(' + c.challengeId + ')">View</button></td>' +
    "</tr>"
  ).join("");
}

// Basic form validation before submit
function validateChallengeForm() {
  const title = $("title").value.trim();
  const startDate = $("startDate").value;
  const endDate = $("endDate").value;
  const minComp = parseInt($("minCompleteness").value, 10);

  if (!title) return "Title is required.";
  if (!startDate) return "Start date is required.";
  if (!endDate) return "End date is required.";
  if (isNaN(minComp) || minComp < 0 || minComp > 100) return "Min completeness must be 0-100.";
  return null;
}

// POST create challenge
async function handleCreateChallenge() {
  $("createMsg").textContent = "";

  const err = validateChallengeForm();
  if (err) {
    $("createMsg").className = "msg-err";
    $("createMsg").textContent = err;
    return;
  }

  const body = {
    title: $("title").value.trim(),
    category: $("category").value,
    startDate: $("startDate").value,
    endDate: $("endDate").value,
    minCompleteness: String(parseInt($("minCompleteness").value, 10) / 100)
  };

  try {
    const res = await fetch("/api/challenges", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": "Bearer " + token
      },
      body: JSON.stringify(body)
    });
    const json = await res.json();

    if (json.success) {
      $("createMsg").className = "msg-ok";
      $("createMsg").textContent = "Challenge created! ID: " + json.data.challengeId;
      resetForm();
      loadChallenges();
    } else {
      $("createMsg").className = "msg-err";
      $("createMsg").textContent = json.error?.message || "Create failed";
    }
  } catch (e) {
    $("createMsg").className = "msg-err";
    $("createMsg").textContent = "Network error: " + e.message;
  }
}

// Fetch and display challenge detail
async function loadChallengeDetail(id) {
  $("detailPlaceholder").style.display = "none";
  $("detailSection").style.display = "block";
  $("detailSection").innerHTML = "Loading...";

  try {
    const res = await fetch("/api/challenges/" + id);
    const json = await res.json();

    if (json.success) {
      const c = json.data;
      $("detailSection").innerHTML =
        "<p><strong>ID:</strong> " + c.challengeId + "</p>" +
        "<p><strong>Title:</strong> " + esc(c.title) + "</p>" +
        "<p><strong>Category:</strong> " + esc(c.category) + "</p>" +
        "<p><strong>Start Date:</strong> " + esc(c.startDate) + "</p>" +
        "<p><strong>End Date:</strong> " + esc(c.endDate) + "</p>" +
        "<p><strong>Constraints:</strong> " + esc(c.constraints) + "</p>" +
        "<p><strong>Scoring Rules:</strong> " + esc(c.scoringRules) + "</p>" +
        "<p><strong>Created By (userId):</strong> " + c.createdBy + "</p>" +
        "<p><strong>Created At:</strong> " + esc(c.createdAt) + "</p>";
    } else {
      $("detailSection").innerHTML = '<p class="msg-err">' + (json.error?.message || "Not found") + '</p>';
    }
  } catch (e) {
    $("detailSection").innerHTML = '<p class="msg-err">Network error: ' + e.message + '</p>';
  }
}

// Reset form fields
function resetForm() {
  $("title").value = "";
  $("startDate").value = "";
  $("endDate").value = "";
  $("minCompleteness").value = "80";
  $("category").selectedIndex = 0;
}

// Escape HTML to prevent XSS
function esc(str) {
  if (str == null) return "";
  return String(str).replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
}

// Event listeners
$("createBtn").addEventListener("click", handleCreateChallenge);
$("resetBtn").addEventListener("click", function () { resetForm(); $("createMsg").textContent = ""; });
document.addEventListener("DOMContentLoaded", initChallengeSection);
