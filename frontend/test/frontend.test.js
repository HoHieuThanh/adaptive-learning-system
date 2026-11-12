import { describe, it } from 'node:test';
import assert from 'node:assert';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const rootDir = path.resolve(__dirname, '..');

describe('Frontend Project Structure & Configuration Tests', () => {
  it('should have valid package.json with dependencies', () => {
    const pkgPath = path.join(rootDir, 'package.json');
    assert.ok(fs.existsSync(pkgPath), 'package.json must exist');
    const pkg = JSON.parse(fs.readFileSync(pkgPath, 'utf8'));
    assert.strictEqual(pkg.name, 'adaptive-learning-frontend');
    assert.ok(pkg.dependencies.react, 'react dependency must be defined');
    assert.ok(pkg.dependencies['react-router-dom'], 'react-router-dom must be defined');
    assert.ok(pkg.dependencies.axios, 'axios must be defined');
  });

  it('should have index.html with #root element', () => {
    const indexPath = path.join(rootDir, 'index.html');
    assert.ok(fs.existsSync(indexPath), 'index.html must exist');
    const content = fs.readFileSync(indexPath, 'utf8');
    assert.ok(content.includes('id="root"'), 'index.html must have #root div');
    assert.ok(content.includes('src="/src/main.jsx"'), 'index.html must load main.jsx');
  });

  it('should have required API service modules', () => {
    const apiDir = path.join(rootDir, 'src', 'api');
    const requiredFiles = [
      'client.js',
      'authService.js',
      'studentService.js',
      'predictionService.js',
      'learningPathService.js',
      'dashboardService.js',
      'gradeService.js',
      'subjectService.js',
      'mockData.js'
    ];
    for (const file of requiredFiles) {
      assert.ok(fs.existsSync(path.join(apiDir, file)), `API file ${file} must exist`);
    }
  });

  it('should validate mockData integrity for offline fallback', async () => {
    const mockDataModule = await import('../src/api/mockData.js');
    assert.ok(mockDataModule.MOCK_STUDENTS, 'MOCK_STUDENTS must be exported');
    assert.ok(Array.isArray(mockDataModule.MOCK_STUDENTS), 'MOCK_STUDENTS must be an array');
    assert.ok(mockDataModule.MOCK_STUDENTS.length >= 20, 'MOCK_STUDENTS must contain at least 20 students');
    
    // Check structure of first student
    const s = mockDataModule.MOCK_STUDENTS[0];
    assert.ok(s.id, 'student must have id');
    assert.ok(s.studentCode, 'student must have studentCode');
    assert.ok(s.fullName, 'student must have fullName');

    // Check subjects
    assert.ok(mockDataModule.MOCK_SUBJECTS, 'MOCK_SUBJECTS must be exported');
    assert.ok(mockDataModule.MOCK_SUBJECTS.length >= 4, 'Must have at least 4 subjects');
  });
});
