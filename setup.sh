#!/bin/bash
# SETUP SCRIPT CHO SUBMISSION MODULE

echo "=== OJ AI Tester - Submission Module Setup ==="
echo ""

# Kiểm tra g++
echo "1. Checking g++ compiler..."
if command -v g++ &> /dev/null; then
    g++ --version | head -n 1
    echo "✓ g++ found"
else
    echo "✗ g++ not found!"
    echo "Please install MinGW-w64 from: https://www.mingw-w64.org/"
    exit 1
fi

echo ""

# Kiểm tra Java
echo "2. Checking Java..."
if command -v java &> /dev/null; then
    java -version 2>&1 | head -n 1
    echo "✓ Java found"
else
    echo "✗ Java not found!"
    exit 1
fi

echo ""

# Kiểm tra Maven
echo "3. Checking Maven..."
if command -v mvn &> /dev/null; then
    mvn --version | head -n 1
    echo "✓ Maven found"
else
    echo "✗ Maven not found!"
    exit 1
fi

echo ""

# Tạo thư mục
echo "4. Creating directories..."
mkdir -p submissions/testcases
echo "✓ Directory created: submissions/testcases"

echo ""

# Build project
echo "5. Building project..."
mvn clean package -DskipTests

if [ $? -eq 0 ]; then
    echo "✓ Build successful!"
else
    echo "✗ Build failed!"
    exit 1
fi

echo ""
echo "=== Setup Complete ==="
echo ""
echo "To run the application:"
echo "  mvn javafx:run"
echo ""

