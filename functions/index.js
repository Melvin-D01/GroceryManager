const functions = require('firebase-functions');
const axios = require('axios');
const admin = require('firebase-admin');
const { HttpsError } = require('firebase-functions/v2/https');

// Initialize Firebase Admin SDK
if (!admin.apps.length) {
    admin.initializeApp();
}

const db = admin.firestore();

exports.callOpenAIForRecipes = functions.https.onCall(async (data, context) => {
    const API_URL = "https://api.openai.com/v1/completions";
    const API_KEY = functions.config().openai.key;

    // Ensure the user is authenticated
    if (!context.auth) {
        throw new HttpsError('unauthenticated', 'User must be authenticated.');
    }

    const userId = context.auth.uid;
    const userCollection = db.collection('users').doc(userId).collection('food');
    const currentDate = admin.firestore.Timestamp.now();

    let expiringItemsList = [];

    try {
        const snapshot = await userCollection
            .where('ExpirationDate', '>', currentDate)
            .orderBy('ExpirationDate', 'ASC')
            .limit(10)
            .get();

        snapshot.forEach(document => {
            const foodItem = document.data();
            expiringItemsList.push(foodItem.Description);
        });
    } catch (error) {
        throw new HttpsError('unknown', 'Failed to fetch items from Firestore: ' + error.message);
    }

    const promptText = `Provide a recipe for a dish using ${expiringItemsList.join(", ")}. Make sure the title has *** on each side to highlight it.`;

    try {
        const response = await axios.post(API_URL, {
            prompt: promptText,
            model: "gpt-3.5-turbo-instruct",
            max_tokens: 500,
            temperature: 0
        }, {
            headers: {
                'Authorization': `Bearer ${API_KEY}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.data && response.data.choices && response.data.choices[0] && response.data.choices[0].text) {
            return response.data.choices[0].text;
        } else {
            throw new Error("Unexpected response structure from OpenAI.");
        }
    } catch (error) {
        throw new HttpsError('unknown', 'Error calling OpenAI: ' + error.message);
    }
});
