#!/bin/bash

# Phynance Development Environment Quick Start
# Sets up both Loveable and Cursor development workflows

echo "🚀 Setting up Phynance Development Environment"
echo "=============================================="

# Check if we're in the right directory
if [ ! -d "phynance" ]; then
    echo "❌ Error: Please run this script from the root phynance directory"
    exit 1
fi

echo "📁 Creating additional directory structure..."
mkdir -p frontend-loveable/{components/{Dashboard,Analysis,Charts,Common},pages,assets}
mkdir -p integration/shared-types
mkdir -p ml-models/{data,notebooks,trained-models}

echo "📋 Current project structure:"
echo "phynance/"
echo "├── backend-spring/          # 💻 CURSOR: Spring Boot backend (Port 8081)"
echo "├── frontend/                # 💻 CURSOR: Production React frontend (Port 3000)"
echo "├── frontend-loveable/       # 🎨 LOVEABLE: UI development & design"
echo "├── ml-models/              # 💻 CURSOR: Model training & data science"
echo "├── integration/            # 🔄 SHARED: API contracts & documentation"
echo "└── docs/workflows/         # 📖 DOCUMENTATION"

echo ""
echo "🔧 Available Development Commands:"
echo ""

echo "🎨 LOVEABLE Frontend Development:"
echo "  cd frontend-loveable/"
echo "  # Use Loveable to design components"
echo "  # Export components to this directory"
echo "  # Test with mock data from mock-data/sample-responses.js"

echo ""
echo "💻 CURSOR Backend Development:"
echo "  # Terminal 1: Start Spring Boot backend"
echo "  cd phynance/backend-spring"
echo "  mvn spring-boot:run"
echo ""
echo "  # Terminal 2: Start React frontend"
echo "  cd phynance/frontend"
echo "  npm install && npm start"

echo ""
echo "🧪 Testing Integration:"
echo "  # Test backend health"
echo "  curl http://localhost:8081/api/v1/viewer/health"
echo ""
echo "  # Test physics analysis"
echo "  curl -X POST http://localhost:8081/api/v1/analysis/harmonic-oscillator \\"
echo "    -H 'Content-Type: application/json' \\"
echo "    -d '{\"symbol\": \"AAPL\"}'"

echo ""
echo "📚 Documentation:"
echo "  - Workflow Guide: docs/workflows/LOVEABLE_CURSOR_WORKFLOW.md"
echo "  - API Specification: integration/api-contracts/phynance-api-spec.json"
echo "  - Loveable Setup: frontend-loveable/README.md"

echo ""
echo "🔄 Integration Workflow:"
echo "  1. Design UI in Loveable → Export to frontend-loveable/"
echo "  2. Develop backend/models in Cursor → backend-spring/"
echo "  3. Integrate components → Copy to frontend/"
echo "  4. Test end-to-end → Both tools working together"

echo ""
echo "✅ Quick Start Checklist:"

# Check if backend dependencies are installed
if [ -f "phynance/backend-spring/pom.xml" ]; then
    echo "  ✅ Spring Boot backend configured"
else
    echo "  ❌ Spring Boot backend missing"
fi

# Check if frontend dependencies are available
if [ -f "phynance/frontend/package.json" ]; then
    echo "  ✅ React frontend configured"
else
    echo "  ❌ React frontend missing"
fi

# Check if integration files are created
if [ -f "integration/api-contracts/phynance-api-spec.json" ]; then
    echo "  ✅ API contracts defined"
else
    echo "  ❌ API contracts missing"
fi

# Check if mock data is available
if [ -f "frontend-loveable/mock-data/sample-responses.js" ]; then
    echo "  ✅ Mock data for Loveable development ready"
else
    echo "  ❌ Mock data missing"
fi

echo ""
echo "🎯 Next Steps:"
echo ""
echo "FOR LOVEABLE (UI Design):"
echo "  1. Open Loveable and create a new project"
echo "  2. Import mock data from frontend-loveable/mock-data/"
echo "  3. Design components using the guidelines in frontend-loveable/README.md"
echo "  4. Export finished components to frontend-loveable/components/"

echo ""
echo "FOR CURSOR (Backend Development):"
echo "  1. cd phynance/backend-spring && mvn spring-boot:run"
echo "  2. Open Cursor in this workspace"
echo "  3. Develop new features, optimize physics models"
echo "  4. Test APIs using the existing frontend at localhost:3000"

echo ""
echo "🔥 Ready to start building! Choose your tool:"
echo "  🎨 Loveable for beautiful UI design"
echo "  💻 Cursor for powerful backend development"
echo ""
echo "📖 Read docs/workflows/LOVEABLE_CURSOR_WORKFLOW.md for detailed instructions"

# Make the script executable
chmod +x integration/quick-start.sh 