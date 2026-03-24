const { DatabaseSync } = require("node:sqlite");
const path = require("path");

const dbPath = path.join(__dirname, "barangay.db");

let sqlite;
try {
  sqlite = new DatabaseSync(dbPath);
  console.log("Connected to SQLite database.");
} catch (err) {
  console.error("Database connection error:", err.message);
  throw err;
}

sqlite.exec("PRAGMA busy_timeout = 5000");

function normalizeArgs(params, callback) {
  if (typeof params === "function") {
    return { params: [], callback: params };
  }

  if (params === undefined) {
    return { params: [], callback };
  }

  return { params: Array.isArray(params) ? params : [params], callback };
}

function asyncCallback(callback, context, err, result) {
  if (!callback) {
    if (err) throw err;
    return result;
  }

  setImmediate(() => callback.call(context, err, result));
  return undefined;
}

const db = {
  serialize(fn) {
    fn();
  },

  run(sql, params, callback) {
    const { params: values, callback: cb } = normalizeArgs(params, callback);

    try {
      const stmt = sqlite.prepare(sql);
      const info = stmt.run(...values);
      return asyncCallback(
        cb,
        {
          lastID: Number(info.lastInsertRowid || 0),
          changes: Number(info.changes || 0),
        },
        null
      );
    } catch (err) {
      return asyncCallback(cb, null, err);
    }
  },

  get(sql, params, callback) {
    const { params: values, callback: cb } = normalizeArgs(params, callback);

    try {
      const stmt = sqlite.prepare(sql);
      const row = stmt.get(...values);
      return asyncCallback(cb, null, null, row);
    } catch (err) {
      return asyncCallback(cb, null, err);
    }
  },

  all(sql, params, callback) {
    const { params: values, callback: cb } = normalizeArgs(params, callback);

    try {
      const stmt = sqlite.prepare(sql);
      const rows = stmt.all(...values);
      return asyncCallback(cb, null, null, rows);
    } catch (err) {
      return asyncCallback(cb, null, err);
    }
  },
};

db.serialize(() => {
  db.run(`CREATE TABLE IF NOT EXISTS users(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    fullName TEXT,
    email TEXT UNIQUE,
    password TEXT,
    address TEXT,
    role TEXT DEFAULT 'resident'
  )`);

  db.run(`CREATE TABLE IF NOT EXISTS document_requests(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    userId INTEGER,
    doc TEXT,
    date TEXT,
    time TEXT,
    status TEXT DEFAULT 'Pending',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
  )`);

  db.run(`CREATE TABLE IF NOT EXISTS complaints(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    userId INTEGER,
    cat TEXT,
    subj TEXT,
    desc TEXT,
    status TEXT DEFAULT 'Pending',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
  )`);

  db.run(`CREATE TABLE IF NOT EXISTS notifications(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    userId INTEGER,
    message TEXT,
    time TEXT
  )`);

  db.run(`CREATE TABLE IF NOT EXISTS announcements(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT,
    content TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
  )`);
});

module.exports = db;
