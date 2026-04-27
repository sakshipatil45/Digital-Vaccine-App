const { admin, db, auth } = require("./config/firebase");

const SAMPLE_USERS = [
    { name: "Rahul Sharma", phone: "9000000001" },
    { name: "Priya Singh", phone: "9000000002" },
    { name: "Amit Patel", phone: "9000000003" },
    { name: "Sneha Reddy", phone: "9000000004" },
    { name: "Vikram Malhotra", phone: "9000000005" }
];

async function clearAuthUsers() {
    console.log("Fetching Auth Users...");
    let nextPageToken;
    let usersToDelete = [];
    do {
        const listUsersResult = await auth.listUsers(1000, nextPageToken);
        listUsersResult.users.forEach(userRecord => {
            if (userRecord.email !== "admitn@gmail.com") {
                usersToDelete.push(userRecord.uid);
            }
        });
        nextPageToken = listUsersResult.pageToken;
    } while (nextPageToken);

    if (usersToDelete.length > 0) {
        console.log(`Deleting ${usersToDelete.length} users from Auth...`);
        await auth.deleteUsers(usersToDelete);
    } else {
        console.log("No non-admin users found in Auth.");
    }
}

async function clearCollection(collectionPath) {
    const query = db.collection(collectionPath).limit(50);
    return new Promise((resolve, reject) => {
        deleteQueryBatch(db, query, resolve).catch(reject);
    });
}

async function deleteQueryBatch(db, query, resolve) {
    const snapshot = await query.get();
    const batchSize = snapshot.size;
    if (batchSize === 0) {
        resolve();
        return;
    }
    const batch = db.batch();
    snapshot.docs.forEach((doc) => {
        batch.delete(doc.ref);
    });
    await batch.commit();
    process.nextTick(() => {
        deleteQueryBatch(db, query, resolve);
    });
}

async function seedData() {
    try {
        console.log("Starting data wipe...");
        await clearAuthUsers();
        
        console.log("Clearing Firestore collections...");
        const collections = ["users", "family_members", "vaccinations", "notifications", "campaign_reminders"];
        for (const col of collections) {
            await clearCollection(col);
            console.log(`Cleared collection: ${col}`);
        }

        // Re-insert/Create admin
        let adminUser;
        try {
            adminUser = await auth.getUserByEmail("admitn@gmail.com");
            console.log("Found existing admin user.");
        } catch (e) {
            console.log("Admin admitn@gmail.com not found. Creating new admin user...");
            adminUser = await auth.createUser({
                email: "admitn@gmail.com",
                password: "adminpassword", // Set a fixed password for admin
                displayName: "System Admin"
            });
        }

        if (adminUser) {
            await db.collection("users").doc(adminUser.uid).set({
                userId: adminUser.uid,
                name: "Admin User",
                email: "admitn@gmail.com",
                role: "admin",
                createdAt: admin.firestore.FieldValue.serverTimestamp()
            });
            console.log("Set Admin document in users collection.");
        }

        console.log("Seeding sample citizens...");
        for (const u of SAMPLE_USERS) {
            const email = u.phone + "@digitalvaccine.com";
            console.log(`Creating auth user: ${u.phone}`);
            
            const userRecord = await auth.createUser({
                email: email,
                password: "password123",
                displayName: u.name
            });

            const uid = userRecord.uid;

            await db.collection("users").doc(uid).set({
                userId: uid,
                name: u.name,
                phone: u.phone,
                role: "citizen",
                createdAt: admin.firestore.FieldValue.serverTimestamp()
            });

            console.log(`Created Citizen document: ${u.name}`);

            // Add 2-3 Family Members per user
            const memberCount = Math.floor(Math.random() * 2) + 2; // 2 or 3
            const CATEGORIES = ["0–1 year", "1–5 years", "6–12 years", "Pregnant Women", "18+ years"];
            const MALE_NAMES = ["Aarav", "Vihaan", "Aditya", "Sai", "Arjun", "Ishaan", "Raj", "Suresh", "Rohan", "Kabir"];
            const FEMALE_NAMES = ["Anaya", "Diya", "Saanvi", "Myra", "Avni", "Kavya", "Meena", "Geeta", "Pooja", "Riya"];

            for (let i = 0; i < memberCount; i++) {
                const cat = CATEGORIES[Math.floor(Math.random() * CATEGORIES.length)];
                const gender = (cat === "Pregnant Women" ? "Female" : (i % 2 === 0 ? "Male" : "Female"));
                
                let randomFirstName;
                if (gender === "Male") {
                    randomFirstName = MALE_NAMES[Math.floor(Math.random() * MALE_NAMES.length)];
                } else {
                    randomFirstName = FEMALE_NAMES[Math.floor(Math.random() * FEMALE_NAMES.length)];
                }
                
                const memberName = randomFirstName + " " + u.name.split(" ")[1]; // Use parent's surname
                
                const memberData = {
                    memberId: "", // Will be set after add
                    userId: uid,
                    name: memberName,
                    age: (cat === "Pregnant Women" ? "25" : (i * 3 + 2).toString()),
                    gender: gender,
                    category: cat,
                    isPregnant: (cat === "Pregnant Women"),
                    createdAt: admin.firestore.FieldValue.serverTimestamp()
                };

                if (cat.includes("year")) {
                    memberData.motherName = "Savitri " + u.name.split(" ")[1];
                    memberData.fatherName = u.name;
                } else if (cat === "Pregnant Women") {
                    memberData.husbandName = u.name;
                }

                const docRef = await db.collection("family_members").add(memberData);
                await docRef.update({ memberId: docRef.id });
                console.log(`  Added family member: ${memberName} (${cat}) - ID: ${docRef.id}`);
            }
        }

        console.log("Seeding complete! You can log in using any sample phone number (e.g. 9000000001) and password 'password123'");
        process.exit(0);
    } catch (error) {
        console.error("Error seeding data:", error);
        process.exit(1);
    }
}

seedData();
