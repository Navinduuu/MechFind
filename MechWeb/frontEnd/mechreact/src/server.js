// 1. IMPORTS
const express = require('express');
const cors = require('cors');
const admin = require('firebase-admin');
const serviceAccount = require('./serviceAccountKey.json');

// 2. INITIALIZE FIREBASE ADMIN
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});
const db = admin.firestore();

// 3. INITIALIZE EXPRESS & MIDDLEWARE
const app = express();

app.use(cors({
  origin: 'http://localhost:5173',
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization']
}));

app.use(express.json());

// 4. ROUTES
// Payment Route
app.post('/api/mechfind/payments/process', async (req, res) => {
  try {
    // Payment handling logic here
    res.status(200).json({ success: true, message: "Payment processed successfully" });
  } catch (error) {
    res.status(500).json({ success: false, error: error.message });
  }
});

// Mechanic Update Route (Firebase Firestore)
app.put('/api/mech/:id', async (req, res) => {
  try {
    await db.collection('mechanics').doc(req.params.id).update(req.body);
    res.status(200).json({ success: true, message: "Updated successfully" });
  } catch (error) {
    res.status(500).json({ success: false, error: error.message });
  }
});

// 5. SERVER LISTEN
const PORT = 8085;
app.listen(PORT, () => {
  console.log(`Server running on http://localhost:${PORT}`);
});