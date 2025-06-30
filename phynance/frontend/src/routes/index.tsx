import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import UsersPage from "../pages/UsersPage";

const AppRoutes: React.FC = () => (
  <Router>
    <Routes>
      <Route path="/users" element={<UsersPage />} />
      <Route path="/" element={<UsersPage />} />
    </Routes>
  </Router>
);

export default AppRoutes;
