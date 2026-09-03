#!/bin/bash

# Build and package the Classroom MCP Server
# This script compiles, tests, and creates a distribution zip file

set -e

echo "================================================="
echo "Classroom MCP Server - Build and Package Script"
echo "================================================="
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed. Please install Maven and try again."
    exit 1
fi

echo "[1/3] Cleaning previous builds..."
mvn clean

echo ""
echo "[2/3] Building and testing..."
mvn install -DskipTests=true

echo ""
echo "[3/3] Creating distribution package..."
mvn assembly:single

echo ""
echo "================================================="
echo "Build complete!"
echo "================================================="
echo ""
echo "Generated files:"
echo "  - Application JAR: target/classroom-mcp-server-1.0.0.jar"
echo "  - Distribution ZIP: target/classroom-mcp-server-1.0.0-dist.zip"
echo ""
echo "To run the application:"
echo "  java -jar target/classroom-mcp-server-1.0.0.jar"
echo ""
echo "================================================="
