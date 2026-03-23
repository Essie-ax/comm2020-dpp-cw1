// Player page: list challenges, submit passport, view results

const submissions = []; // track submissions in this session

function initPlayerPage() {
  const token = localStorage.getItem("token");
  const role = localStorage.getItem("role");
  if (!token) { window.location.href = "/login.html"; return; }

  document.getElementById("info").textContent = "Logged in as Player (role=" + role + ")";
  document.getElementById("logout").addEventListener("click", function () {
    localStorage.clear();
  });

  loadChallenges(token);
  document.getElementById("submitBtn").addEventListener("click", function () {
    submitPassport(token);
  });
}

// Load available challenges
async function loadChallenges(token) {
  try {
    const res = await fetch("/api/challenges", {
      headers: { "Authorization": "Bearer " + token }
    });
    const json = await res.json();
    if (!json.success) {
      document.getElementById("challengeMsg").innerHTML = '<p class="msg-err">' + esc(json.error?.message || "Failed to load") + '</p>';
      return;
    }
    const list = json.data?.challenges || [];
    renderChallenges(list);
  } catch (e) {
    document.getElementById("challengeMsg").innerHTML = '<p class="msg-err">Network error loading challenges</p>';
  }
}

function renderChallenges(list) {
  const tbody = document.getElementById("challengeList");
  if (list.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6" style="color:#999;text-align:center">No challenges available</td></tr>';
    return;
  }
  tbody.innerHTML = list.map(function (c) {
    var cid = c.challengeId || c.id;
    return '<tr>' +
      '<td>' + esc(String(cid)) + '</td>' +
      '<td>' + esc(c.title || "") + '</td>' +
      '<td>' + esc(c.category || "") + '</td>' +
      '<td>' + esc(c.startDate || "") + '</td>' +
      '<td>' + esc(c.endDate || "") + '</td>' +
      '<td><button class="btn-primary" onclick="selectChallenge(' + cid + ')">Select</button></td>' +
      '</tr>';
  }).join("");
}

// Click "Select" fills in the challenge ID
function selectChallenge(id) {
  document.getElementById("challengeId").value = id;
  document.getElementById("passportId").focus();
}

// Submit passport to challenge
async function submitPassport(token) {
  var msgEl = document.getElementById("submitMsg");
  msgEl.innerHTML = "";

  var challengeId = document.getElementById("challengeId").value.trim();
  var passportId = document.getElementById("passportId").value.trim();

  // Validate input
  if (!challengeId || parseInt(challengeId) <= 0) {
    msgEl.innerHTML = '<p class="msg-err">Please enter a valid Challenge ID (positive number)</p>';
    return;
  }
  if (!passportId || parseInt(passportId) <= 0) {
    msgEl.innerHTML = '<p class="msg-err">Please enter a valid Passport ID (positive number)</p>';
    return;
  }

  msgEl.innerHTML = '<p style="color:#7f8c8d">Submitting...</p>';

  try {
    var res = await fetch("/api/challenges/" + challengeId + "/submit", {
      method: "POST",
      headers: {
        "Authorization": "Bearer " + token,
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ passportId: passportId })
    });

    var json = await res.json();

    if (!json.success) {
      msgEl.innerHTML = '<p class="msg-err">Error: ' + esc(json.error?.message || "Submission failed") + '</p>';
      document.getElementById("resultSection").classList.add("hidden");
      return;
    }

    msgEl.innerHTML = '<p class="msg-ok">Submission successful!</p>';
    showResult(json.data);
    addToHistory(json.data);

  } catch (e) {
    msgEl.innerHTML = '<p class="msg-err">Network error. Is the server running?</p>';
  }
}

// Display submission result
function showResult(data) {
  var section = document.getElementById("resultSection");
  section.classList.remove("hidden");

  document.getElementById("resScore").textContent = data.score;
  
  var outcomeEl = document.getElementById("resOutcome");
  if (data.outcome === "PASS") {
    outcomeEl.innerHTML = '<span class="badge-pass">PASS</span>';
  } else {
    outcomeEl.innerHTML = '<span class="badge-fail">FAIL</span>';
  }

  document.getElementById("resSource").innerHTML = "";

  document.getElementById("resChallengeId").textContent = data.challengeId;
  document.getElementById("resPassportId").textContent = data.passportId;
  document.getElementById("resSubId").textContent = data.submissionId;

  // Render feedback list
  var feedbackEl = document.getElementById("resFeedback");
  var feedbackArr = data.feedback || [];
  feedbackEl.innerHTML = feedbackArr.map(function (f) {
    return '<li>' + esc(f) + '</li>';
  }).join("");
}

// Track submission history in this session
function addToHistory(data) {
  submissions.push(data);
  renderHistory();
}

function renderHistory() {
  var tbody = document.getElementById("historyList");
  tbody.innerHTML = submissions.map(function (s, i) {
    var outcomeCls = s.outcome === "PASS" ? "badge-pass" : "badge-fail";
    return '<tr>' +
      '<td>' + (i + 1) + '</td>' +
      '<td>' + esc(String(s.challengeId)) + '</td>' +
      '<td>' + esc(String(s.passportId)) + '</td>' +
      '<td><strong>' + s.score + '</strong></td>' +
      '<td><span class="' + outcomeCls + '">' + esc(s.outcome) + '</span></td>' +
      '</tr>';
  }).join("");
}

// HTML escape
function esc(s) {
  var d = document.createElement("div");
  d.textContent = s;
  return d.innerHTML;
}

// Start
window.addEventListener("DOMContentLoaded", initPlayerPage);
