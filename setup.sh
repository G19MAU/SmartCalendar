#!/usr/bin/env bash
set -e

echo "Installing root dependencies..."
npm ci

echo "Installing frontend dependencies..."
npm ci --prefix frontend

echo "Building backend..."
(cd backend/backend/app && ./mvnw -q -DskipTests package)

echo "✅ Setup complete!"
