#!/bin/bash
# ============================================================
# V4 PROJECT SETUP SCRIPT
# Run this once after cloning the repo.
# ============================================================

echo "==> Setting up V4 project..."

# 1. Decode debug keystore from base64
if [ -f "debug.keystore.base64" ]; then
    base64 -d debug.keystore.base64 > debug.keystore
    echo "✅ debug.keystore decoded successfully."
else
    echo "⚠️  debug.keystore.base64 not found — skipping keystore setup."
fi

# 2. Create .env file if it doesn't exist
if [ ! -f ".env" ]; then
    cp .env.example .env
    echo ""
    echo "✅ .env file created from template."
    echo ""
    echo "⚠️  IMPORTANT: Open .env and replace MY_GEMINI_API_KEY with your real Gemini API key."
    echo "   Get your key from: https://aistudio.google.com/app/apikey"
    echo ""
else
    echo "✅ .env file already exists — skipping."
fi

echo ""
echo "==> Setup complete! You can now open the project in Android Studio."
echo "    Make sure to set your GEMINI_API_KEY in the .env file before building."
