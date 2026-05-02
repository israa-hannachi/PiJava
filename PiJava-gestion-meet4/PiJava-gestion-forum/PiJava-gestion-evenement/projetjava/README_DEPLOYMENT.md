# 🚀 Deployment Guide - AI Multi-Providers System

## ✅ **Successfully pushed to GitHub!**

**Branch:** `test-israa-clean`  
**Repository:** https://github.com/israa-hannachi/PiJava.git  
**Status:** ✅ Pushed successfully (no API keys detected)

---

## 🔐 **Configuration Setup (Required)**

### 1. **Copy the configuration template**
```bash
cp src/main/resources/config.properties.template src/main/resources/config.properties
```

### 2. **Add your API keys**
Edit `src/main/resources/config.properties`:

```properties
# Remplacez par vos vraies clés API
GEMINI_API_KEY=
OPENAI_API_KEY=
GROQ_API_KEY=
HUGGINGFACE_API_KEY=  # Optionnel
```

---

## 🎯 **Features Included**

### ✨ **AI Multi-Providers System**
- **Groq** (Llama 3.1) - Priority 1 - Free & Fast
- **Hugging Face** (Mistral-7B) - Backup - Free
- **Gemini** - Backup - Google AI
- **OpenAI** (GPT-3.5) - Last resort - Paid

### 📄 **PDF Integration**
- **Cloudinary PDF extraction** with Apache PDFBox
- **Automatic text extraction** from PDF URLs
- **Content combination** (HTML + PDF) for complete summaries

### 🔄 **Intelligent Fallback**
- **Automatic provider switching** on failure
- **Error handling** with detailed logs
- **User-friendly messages** for each scenario

### 🛡️ **Security**
- **API keys excluded** from Git (config.properties in .gitignore)
- **Template provided** for secure setup
- **No hardcoded secrets** in source code

---

## 🚀 **Quick Start**

### 1. **Clone and setup**
```bash
git clone https://github.com/israa-hannachi/PiJava.git
cd PiJava/PiJava-DEV/PiJava-gestion-meet4/PiJava-gestion-forum/PiJava-gestion-evenement/projetjava
```

### 2. **Configure API keys**
```bash
cp src/main/resources/config.properties.template src/main/resources/config.properties
# Edit config.properties with your API keys
```

### 3. **Run the application**
```bash
mvn clean javafx:run
```

### 4. **Test AI Summary**
1. Login as student or professor
2. Go to a course with content (text or PDF)
3. Click "✨ Résumé IA"
4. Watch the magic happen! 🎉

---

## 📋 **API Key Setup**

### 🔑 **Get your API keys**

| Provider | URL | Cost | Setup Time |
|----------|-----|------|------------|
| **Groq** (Recommended) | https://console.groq.com/keys | 💰 Free | 2 min |
| **Gemini** | https://aistudio.google.com/apikey | 💰 Free tier | 2 min |
| **OpenAI** | https://platform.openai.com/api-keys | 💰 Paid | 2 min |
| **Hugging Face** | https://huggingface.co/settings/tokens | 💰 Free | 2 min |

### ⚡ **Groq Setup (Recommended)**
1. Go to https://console.groq.com/keys
2. Create free account
3. Generate API key
4. Add to `config.properties`

---

## 🔍 **Testing Guide**

### Test 1: Course with Text Content
1. Create a course with rich text content
2. Click "✨ Résumé IA"
3. Should generate summary in 1-2 seconds

### Test 2: Course with PDF
1. Upload a PDF to a course
2. Click "✨ Résumé IA"
3. Should extract PDF text and summarize

### Test 3: Empty Course
1. Create empty course
2. Click "✨ Résumé IA"
3. Should show helpful error message

---

## 🛠️ **Troubleshooting**

### "Aucun contenu trouvé"
- **Solution:** Add text content or upload a PDF to the course

### "Clé API invalide"
- **Solution:** Check API keys in config.properties

### "Provider failed"
- **Solution:** System automatically tries next provider

### PDF extraction fails
- **Solution:** Ensure PDF is text-based (not scanned images)

---

## 📊 **Architecture Overview**

```
Frontend (JavaFX)
    ↓
SummaryService (Orchestrator)
    ↓
┌─────────┬─────────┬─────────┬─────────┐
│  Groq   │Hugging  │ Gemini  │ OpenAI  │
│ (Llama) │ Face    │         │ (GPT)   │
└─────────┴─────────┴─────────┴─────────┘
    ↓
PDF Extraction (Apache PDFBox)
    ↓
Cloudinary URLs
```

---

## 🎉 **Success!**

Your AI multi-providers system is now:
- ✅ **Deployed** on GitHub
- ✅ **Secure** (no API keys exposed)
- ✅ **Configurable** with template
- ✅ **Testable** with comprehensive features

**Ready to generate intelligent summaries!** 🚀✨
