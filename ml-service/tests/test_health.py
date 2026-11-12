"""
Unit tests cho health check endpoint trong src/main.py
"""

from src.main import health_check, root


def test_root_endpoint():
    data = root()
    assert "message" in data
    assert data["docs_url"] == "/docs"


def test_health_check_endpoint():
    data = health_check()
    assert data["status"] == "UP"
    assert data["service"] == "ml-service"
    assert data["model_loaded"] is True
    assert "xgboost" in data["available_models"]
