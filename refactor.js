const fs = require('fs');
const path = require('path');

const srcDir = path.join(__dirname, 'app/src/main/java/com/example/digitalvaccineapp');
const manifestPath = path.join(__dirname, 'app/src/main/AndroidManifest.xml');
const layoutDir = path.join(__dirname, 'app/src/main/res/layout');

// Define the absolute mapping of the Android Application Structure
const fileMap = {
    // ---------------- AUTH FACTORY ---------------- //
    'ui/SplashActivity.java': 'auth/SplashActivity.java',
    'ui/WelcomeActivity.java': 'auth/WelcomeActivity.java',
    'ui/LoginActivity.java': 'auth/LoginActivity.java',
    'ui/RegisterActivity.java': 'auth/RegisterActivity.java',
    
    // ---------------- ASHA PERSONA ---------------- //
    'ui/AshaDashboardActivity.java': 'asha/AshaDashboardActivity.java',
    'ui/AshaReportsActivity.java': 'asha/AshaReportsActivity.java',
    'ui/AshaAlertsActivity.java': 'asha/AshaAlertsActivity.java',
    'ui/AddBeneficiaryActivity.java': 'asha/AddBeneficiaryActivity.java',
    'ui/BeneficiaryListActivity.java': 'asha/BeneficiaryListActivity.java',
    'ui/BeneficiaryDetailActivity.java': 'asha/BeneficiaryDetailActivity.java',
    'adapter/BeneficiaryAdapter.java': 'asha/BeneficiaryAdapter.java',
    'models/Beneficiary.java': 'asha/Beneficiary.java',

    // ---------------- CITIZEN PERSONA ---------------- //
    'ui/VaccinationActivity.java': 'citizen/VaccinationActivity.java',
    'ui/FamilyMembersActivity.java': 'citizen/FamilyMembersActivity.java',
    'ui/AddFamilyMemberActivity.java': 'citizen/AddFamilyMemberActivity.java',
    'adapter/FamilyMemberAdapter.java': 'citizen/FamilyMemberAdapter.java',
    'models/FamilyMember.java': 'citizen/FamilyMember.java',
    'ui/ReminderActivity.java': 'citizen/ReminderActivity.java',
    'receiver/ReminderReceiver.java': 'citizen/ReminderReceiver.java',

    // ---------------- SHARED/COMMON ---------------- //
    'ui/RecordsActivity.java': 'shared/RecordsActivity.java',
    'ui/AddVaccinationActivity.java': 'shared/AddVaccinationActivity.java',
    'ui/VaccineDetailActivity.java': 'shared/VaccineDetailActivity.java',
    'adapter/VaccinationAdapter.java': 'shared/VaccinationAdapter.java',
    'adapter/VaccineInfoAdapter.java': 'shared/VaccineInfoAdapter.java',
    'models/Vaccination.java': 'shared/Vaccination.java',
    'models/VaccinationEntity.java': 'shared/VaccinationEntity.java',
    'models/VaccineInfo.java': 'shared/VaccineInfo.java',
    'models/CertificateSummary.java': 'shared/CertificateSummary.java',
    'ui/ProfileActivity.java': 'shared/ProfileActivity.java',
    'ui/CertificateActivity.java': 'shared/CertificateActivity.java',
    'ui/VaccineInfoActivity.java': 'shared/VaccineInfoActivity.java',
    'models/User.java': 'shared/User.java',

    // ---------------- CORE ENGINE ---------------- //
    'network/ApiService.java': 'core/ApiService.java',
    'network/RetrofitClient.java': 'core/RetrofitClient.java',
    'network/TokenInterceptor.java': 'core/TokenInterceptor.java',
    'models/ApiResponse.java': 'core/ApiResponse.java',
    'service/MyFirebaseMessagingService.java': 'core/MyFirebaseMessagingService.java'
};

// 1. Forge target directory structure
const newDirs = ['auth', 'asha', 'citizen', 'shared', 'core'];
newDirs.forEach(d => {
    const dirPath = path.join(srcDir, d);
    if (!fs.existsSync(dirPath)) {
        fs.mkdirSync(dirPath, { recursive: true });
        console.log(`Created domain: ${d}`);
    }
});

// Calculate the absolute java package path for every class mapped above
const classPackageMap = {};
for (const [oldPath, newPath] of Object.entries(fileMap)) {
    const className = path.basename(newPath, '.java');
    const newPackage = 'com.example.digitalvaccineapp.' + path.dirname(newPath).replace(/\\/g, '/').replace(/\//g, '.');
    classPackageMap[className] = newPackage;
}

// 2. Move files and Update internal package declarations
for (const [oldPath, newPath] of Object.entries(fileMap)) {
    const fullOldPath = path.join(srcDir, oldPath);
    const fullNewPath = path.join(srcDir, newPath);
    
    if (fs.existsSync(fullOldPath)) {
        let content = fs.readFileSync(fullOldPath, 'utf8');
        const currentPackage = 'com.example.digitalvaccineapp.' + path.dirname(newPath).replace(/\\/g, '/').replace(/\//g, '.');
        
        // Overwrite the Package declaration
        content = content.replace(/^package com\.example\.digitalvaccineapp\.[a-z]+;/m, `package ${currentPackage};`);
        
        fs.writeFileSync(fullNewPath, content);
        fs.unlinkSync(fullOldPath);
        const className = path.basename(newPath, '.java');
        console.log(`Relocated: ${className} -> ${currentPackage}`);
    }
}

// Helper to recursively get all Java files
function getAllJavaFiles(dir, fileList = []) {
    const files = fs.readdirSync(dir);
    for (const file of files) {
        const fullPath = path.join(dir, file);
        if (fs.statSync(fullPath).isDirectory()) {
            getAllJavaFiles(fullPath, fileList);
        } else if (fullPath.endsWith('.java')) {
            fileList.push(fullPath);
        }
    }
    return fileList;
}

// 3. Global Import Re-binder (Scans ALL Java files to fix references)
const allJavaFiles = getAllJavaFiles(srcDir);
for (const fullPath of allJavaFiles) {
    let content = fs.readFileSync(fullPath, 'utf8');
    let modified = false;

    // Detect the current package of this file to avoid self-imports
    let currentPackageMatch = content.match(/^package (.+);/m);
    let currentPackage = currentPackageMatch ? currentPackageMatch[1] : '';

    for (const [className, pkg] of Object.entries(classPackageMap)) {
        // Replace old explicit imports
        const importRegexStr = `import com\\.example\\.digitalvaccineapp\\.(ui|adapter|models|network|utils|receiver|service)\\.${className};`;
        const importRegex = new RegExp(importRegexStr, 'g');
        if (content.match(importRegex)) {
            content = content.replace(importRegex, `import ${pkg}.${className};`);
            modified = true;
        }

        // If it's referenced but missing an import (because it used to be co-located)
        if (pkg !== currentPackage) {
            const refRegex = new RegExp(`\\b${className}\\b`);
            const classDefRegex = new RegExp(`class\\s+${className}\\b`);
            
            // If the name is mentioned, AND it's not the definition of the class itself
            if (refRegex.test(content) && !classDefRegex.test(content)) {
                const correctImportStr = `import ${pkg}.${className};`;
                if (!content.includes(correctImportStr)) {
                    content = content.replace(/^(package .+;)/m, `$1\nimport ${pkg}.${className};`);
                    modified = true;
                    console.log(`Injected missing cross-domain import for ${className} inside ${path.basename(fullPath)}`);
                }
            }
        }
    }

    if (modified) {
        fs.writeFileSync(fullPath, content);
    }
}

// 4. System Manifest Reconstruction
if (fs.existsSync(manifestPath)) {
    let manifestContent = fs.readFileSync(manifestPath, 'utf8');
    for (const [oldPath, newPath] of Object.entries(fileMap)) {
        const className = path.basename(newPath, '.java');
        const oldDotPath = '.' + path.dirname(oldPath).replace(/\\/g, '/').replace(/\//g, '.') + '.' + className;
        const newDotPath = '.' + path.dirname(newPath).replace(/\\/g, '/').replace(/\//g, '.') + '.' + className;
        
        const oldFull = 'com.example.digitalvaccineapp' + oldDotPath;
        const newFull = 'com.example.digitalvaccineapp' + newDotPath;
        
        manifestContent = manifestContent.replace(new RegExp(oldDotPath.replace(/\./g, '\\.'), 'g'), newDotPath);
        manifestContent = manifestContent.replace(new RegExp(oldFull.replace(/\./g, '\\.'), 'g'), newFull);
    }
    fs.writeFileSync(manifestPath, manifestContent);
    console.log('Synchronized OS Manifest Paths');
}

// 5. Update XML Layout Connectors (tools:context)
if (fs.existsSync(layoutDir)) {
    const layoutFiles = fs.readdirSync(layoutDir);
    for (const file of layoutFiles) {
        if (file.endsWith('.xml')) {
            const fullPath = path.join(layoutDir, file);
            let content = fs.readFileSync(fullPath, 'utf8');
            let modified = false;

            for (const [oldPath, newPath] of Object.entries(fileMap)) {
                const className = path.basename(newPath, '.java');
                const oldDotPath = '.' + path.dirname(oldPath).replace(/\\/g, '/').replace(/\//g, '.') + '.' + className;
                const newDotPath = '.' + path.dirname(newPath).replace(/\\/g, '/').replace(/\//g, '.') + '.' + className;
                
                if (content.includes(oldDotPath)) {
                    content = content.replace(new RegExp(oldDotPath.replace(/\./g, '\\.'), 'g'), newDotPath);
                    modified = true;
                }
            }
            
            if (modified) {
                fs.writeFileSync(fullPath, content);
            }
        }
    }
    console.log('Restructured XML Navigation Tags');
}

console.log('\n✅ Advanced Base Architecture Refactor Completed');
