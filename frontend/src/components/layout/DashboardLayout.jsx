import React from 'react';
import { Sidebar } from './Sidebar';
import { Navbar } from './Navbar';

export const DashboardLayout = ({ children }) => {
  return (
    <div className="app-layout">
      <Sidebar />
      <div className="main-content">
        <Navbar />
        <main className="page-container">
          {children}
        </main>
      </div>
    </div>
  );
};

