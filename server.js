// server.js
const express = require("express");
const session = require("express-session");
const bcrypt = require("bcryptjs");
const path = require("path");
const db = require("./database"); // gamit na connection mula sa database.js

const app = express();
const PORT = 3000;

// ============ MIDDLEWARE ============
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(express.static(path.join(__dirname, "public")));

app.use(
  session({
    secret: "supersecret123",
    resave: false,
    saveUninitialized: false,
    cookie: { maxAge: 24 * 60 * 60 * 1000 }, // 1 day
  })
);

// ================== PASSWORD VALIDATION ==================
function isStrongPassword(password) {
  if (typeof password !== "string" || password.length < 8) {
    return false;
  }

  const hasLowercase = /[a-z]/.test(password);
  const hasUppercase = /[A-Z]/.test(password);
  const hasNumber = /\d/.test(password);
  const hasSpecialCharacter = /[^A-Za-z0-9]/.test(password);

  return hasLowercase && hasUppercase && hasNumber && hasSpecialCharacter;
}

app.get("/api/health", (req, res) => {
  res.json({ ok: true, message: "Server is running" });
});

function requireLogin(req, res, next) {
  if (!req.session.userId) {
    return res.status(401).json({ message: "Login required" });
  }

  next();
}

function requireAdmin(req, res, next) {
  if (!req.session.userId) {
    return res.status(401).json({ message: "Login required" });
  }

  if (req.session.role !== "admin") {
    return res.status(403).json({ message: "Admin access required" });
  }

  next();
}

// ============ AUTH ============

// REGISTER
app.post("/api/register", async (req, res) => {
  const { name, email, password, address } = req.body;
  if (!name || !email || !password)
    return res.status(400).json({ message: "Fill all fields" });

  if (!isStrongPassword(password)) {
    return res.status(400).json({
      message:
        "Password must be at least 8 characters, include uppercase, lowercase, number and special character",
    });
  }

  try {
    const hash = await bcrypt.hash(password, 10);
    db.run(
      `INSERT INTO users(fullName,email,password,address) VALUES(?,?,?,?)`,
      [name, email, hash, address],
      function (err) {
        if (err)
          return res.status(400).json({ message: "Email already exists" });
        res.json({ message: "User registered", userId: this.lastID });
      }
    );
  } catch (e) {
    res.status(500).json({ message: "Server error" });
  }
});

// LOGIN
app.post("/api/login", (req, res) => {
  const { email, password } = req.body;
  db.get(`SELECT * FROM users WHERE email=?`, [email], async (err, user) => {
    if (err || !user) return res.status(400).json({ message: "Invalid login" });

    const match = await bcrypt.compare(password, user.password);
    if (!match) return res.status(400).json({ message: "Invalid login" });

    req.session.userId = user.id;
    req.session.role = user.role;
    res.json({ message: "Logged in", user });
  });
});

// CURRENT USER INFO
app.get("/api/me", requireLogin, (req, res) => {
  db.get(`SELECT id, fullName, email, address, role FROM users WHERE id=?`, [req.session.userId], (err, user) => {
    if (err || !user) return res.status(400).json({ message: "User not found" });

    // Fetch requests and complaints
    db.all(`SELECT * FROM document_requests WHERE userId=?`, [user.id], (err, requests) => {
      if (err) requests = [];
      db.all(`SELECT * FROM complaints WHERE userId=?`, [user.id], (err, complaints) => {
        if (err) complaints = [];
        db.all(`SELECT * FROM notifications WHERE userId=? ORDER BY time DESC`, [user.id], (err, notifications) => {
          if (err) notifications = [];
          res.json({ ...user, requests, complaints, notifications });
        });
      });
    });
  });
});

app.get("/api/notifications", requireLogin, (req, res) => {
  db.all(
    `SELECT * FROM notifications WHERE userId=? ORDER BY time DESC`,
    [req.session.userId],
    (err, notifications) => {
      if (err) return res.status(500).json({ message: "Failed to load notifications" });
      res.json(notifications || []);
    }
  );
});

// LOGOUT
app.post("/api/logout", (req, res) => {
  req.session.destroy();
  res.json({ message: "Logged out" });
});

// ============ DOCUMENT REQUEST ============
app.post("/api/request", requireLogin, (req, res) => {
  const { doc, date, time } = req.body;
  if (!doc || !date || !time)
    return res.status(400).json({ message: "Missing fields" });

  db.run(
    `INSERT INTO document_requests(userId,doc,date,time) VALUES(?,?,?,?)`,
    [req.session.userId, doc, date, time],
    function (err) {
      if (err) return res.status(500).json({ message: "Database error" });
      db.all(
        `SELECT * FROM document_requests WHERE userId=?`,
        [req.session.userId],
        (err, requests) => {
          res.json({ requests });
        }
      );
    }
  );
});

// ============ COMPLAINT ============
app.post("/api/complaint", requireLogin, (req, res) => {
  const { cat, subj, desc } = req.body;
  if (!cat || !subj || !desc)
    return res.status(400).json({ message: "Missing fields" });

  db.run(
    `INSERT INTO complaints(userId,cat,subj,desc) VALUES(?,?,?,?)`,
    [req.session.userId, cat, subj, desc],
    function (err) {
      if (err) return res.status(500).json({ message: "Database error" });
      db.all(
        `SELECT * FROM complaints WHERE userId=?`,
        [req.session.userId],
        (err, complaints) => {
          res.json({ complaints });
        }
      );
    }
  );
});

// ============ ANNOUNCEMENTS ============
app.get("/api/announcements", (req, res) => {
  db.all(`SELECT * FROM announcements ORDER BY created_at DESC`, [], (err, rows) => {
    if (err) return res.json([]);
    res.json(rows);
  });
});

// ============ TIME SLOT CHECK ============
app.get("/api/checkSlot", (req, res) => {
  const { date, time } = req.query;
  db.get(
    `SELECT COUNT(*) as count FROM document_requests WHERE date=? AND time=?`,
    [date, time],
    (err, row) => {
      if (err) return res.json({ booked: false });
      res.json({ booked: row.count > 0 });
    }
  );
});

// ============ PROFILE UPDATE ============
app.post("/api/updateProfile", requireLogin, (req, res) => {
  const { fullName, email, address } = req.body;

  db.run(
    `UPDATE users SET fullName=?, email=?, address=? WHERE id=?`,
    [fullName, email, address, req.session.userId],
    function (err) {
      if (err) return res.status(500).json({ message: "Update failed" });
      db.get(`SELECT id, fullName, email, address, role FROM users WHERE id=?`, [req.session.userId], (err, user) => {
        res.json(user);
      });
    }
  );
});

// ================== ADMIN ROUTES ==================

// GET ALL DOCUMENT REQUESTS
app.get("/api/admin/me", requireAdmin, (req, res) => {
  db.get(
    "SELECT id, fullName, email, role FROM users WHERE id=?",
    [req.session.userId],
    (err, user) => {
      if (err || !user) return res.status(400).json({ message: "Admin not found" });
      res.json(user);
    }
  );
});

app.get("/api/admin/requests", requireAdmin, (req, res) => {
  db.all(`SELECT * FROM document_requests ORDER BY created_at DESC`, [], (err, rows) => {
    res.json(rows || []);
  });
});

// UPDATE REQUEST STATUS & ADD NOTIFICATION
app.post("/api/admin/update-request", requireAdmin, (req, res) => {
  const { id, status } = req.body;
  db.run(
    "UPDATE document_requests SET status=? WHERE id=?",
    [status, id],
    function (err) {
      if (err) return res.status(500).json({ message: "DB error" });

      db.run(
        `INSERT INTO notifications(userId,message,time) 
        VALUES((SELECT userId FROM document_requests WHERE id=?),?,datetime('now'))`,
        [id, `Your request has been ${status}`]
      );

      res.json({ message: `Request ${status}` });
    }
  );
});

// GET ALL COMPLAINTS
app.get("/api/admin/complaints", requireAdmin, (req, res) => {
  db.all(`SELECT * FROM complaints ORDER BY created_at DESC`, [], (err, rows) => res.json(rows || []));
});

// UPDATE COMPLAINT STATUS & ADD NOTIFICATION
app.post("/api/admin/update-complaint", requireAdmin, (req, res) => {
  const { id, status } = req.body;
  db.run(
    "UPDATE complaints SET status=? WHERE id=?",
    [status, id],
    function (err) {
      if (err) return res.status(500).json({ message: "DB error" });

      db.run(
        `INSERT INTO notifications(userId,message,time) 
        VALUES((SELECT userId FROM complaints WHERE id=?),?,datetime('now'))`,
        [id, `Your complaint has been ${status}`]
      );

      res.json({ message: `Complaint ${status}` });
    }
  );
});

// LIST ALL USERS
app.get("/api/admin/users", requireAdmin, (req, res) => {
  db.all("SELECT id, fullName, email, role FROM users", [], (err, rows) => res.json(rows || []));
});

// ============ START SERVER ============
if (require.main === module) {
  app.listen(PORT, () => console.log(`Server running at http://localhost:${PORT}`));
}

module.exports = app;
