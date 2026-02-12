const $ = (id) => document.getElementById(id);

// Show user info on page load
function initLeaderboardPage() {
  const userId = localStorage.getItem("userId") || "?";
  const role = localStorage.getItem("role") || "?";
  $("info").textContent = "Logged in as: " + userId + " | role: " + role;
}

// Validate challenge id input
function validateChallengeId() {
  const val = $("challengeIdInput").value.trim();
  if (!val) return "Please enter a Challenge ID.";
  const num = parseInt(val, 10);
  if (isNaN(num) || num <= 0) return "Challenge ID must be a positive number.";
  return null;
}

// Fetch leaderboard data from backend
async function loadLeaderboard() {
  $("statusMsg").textContent = "";
  $("emptyState").style.display = "none";
  $("leaderboardTable").style.display = "none";

  const err = validateChallengeId();
  if (err) {
    showError(err);
    return;
  }

  const challengeId = $("challengeIdInput").value.trim();
  $("statusMsg").textContent = "Loading...";
  $("statusMsg").className = "msg-info";

  try {
    const res = await fetch("/api/leaderboard/challenge/" + challengeId);
    const json = await res.json();

    if (json.success && json.data) {
      const entries = json.data.entries || [];
      if (entries.length === 0) {
        renderEmptyState();
      } else {
        renderLeaderboardTable(entries);
      }
      $("statusMsg").textContent = json.data.message || "";
      $("statusMsg").className = "msg-info";
    } else {
      showError(json.error?.message || "Failed to load leaderboard");
    }
  } catch (e) {
    showError("Network error: " + e.message);
  }
}

// Render leaderboard table rows
function renderLeaderboardTable(rows) {
  $("leaderboardTable").style.display = "table";
  $("emptyState").style.display = "none";

  const tbody = $("leaderboardBody");
  tbody.innerHTML = rows.map(function (r) {
    const rankClass = r.rank <= 3 ? "rank-" + r.rank : "";
    const outcomeClass = r.outcome === "PASS" ? "pass" : "fail";
    return "<tr>" +
      '<td class="' + rankClass + '">' + r.rank + "</td>" +
      "<td>" + esc(r.playerId) + "</td>" +
      "<td>" + r.score + "</td>" +
      '<td class="' + outcomeClass + '">' + esc(r.outcome) + "</td>" +
      "<td>" + esc(r.submittedAt) + "</td>" +
    "</tr>";
  }).join("");
}

// Show empty state when no data
function renderEmptyState() {
  $("leaderboardTable").style.display = "none";
  $("emptyState").style.display = "block";
  $("leaderboardBody").innerHTML = "";
}

// Show error message
function showError(message) {
  $("statusMsg").className = "msg-err";
  $("statusMsg").textContent = message;
  $("leaderboardTable").style.display = "none";
  $("emptyState").style.display = "none";
}

// Escape HTML to prevent XSS
function esc(str) {
  if (str == null) return "";
  return String(str).replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
}

// Event listeners
$("loadBtn").addEventListener("click", loadLeaderboard);
$("challengeIdInput").addEventListener("keydown", function (e) {
  if (e.key === "Enter") loadLeaderboard();
});
document.addEventListener("DOMContentLoaded", initLeaderboardPage);
