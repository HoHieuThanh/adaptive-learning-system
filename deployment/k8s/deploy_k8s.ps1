# ==============================================================================
# SCRIPT HO TRO TRIEN KHAI VA KIEM TRA KUBERNETES LOCAL
# He Thong AI-Powered Adaptive Learning & Student Performance Prediction
# ==============================================================================

param(
    [ValidateSet("apply", "delete", "status", "metrics-server", "load-test", "stop-load", "port-forward")]
    [string]$Action = "status"
)

$ErrorActionPreference = "Continue"

switch ($Action) {
    "apply" {
        Write-Host "`n[1/3] Tao Secret cho Kubernetes (neu chua co)..." -ForegroundColor Cyan
        kubectl get secret adaptive-secrets 2>$null
        if ($LASTEXITCODE -ne 0) {
            Write-Host "Dang tao secret adaptive-secrets tu file mau..." -ForegroundColor Yellow
            kubectl apply -f deployment/k8s/secret.yaml.example
        } else {
            Write-Host "Secret adaptive-secrets da ton tai." -ForegroundColor Green
        }

        Write-Host "`n[2/3] Ap dung toan bo Kubernetes Manifests qua Kustomize..." -ForegroundColor Cyan
        kubectl apply -k deployment/k8s

        Write-Host "`n[3/3] Kiem tra trang thai rollout..." -ForegroundColor Cyan
        kubectl rollout status statefulset/postgres-statefulset --timeout=90s
        kubectl rollout status deployment/ml-service-deployment --timeout=90s
        kubectl rollout status deployment/backend-deployment --timeout=90s
        kubectl rollout status deployment/frontend-deployment --timeout=90s

        Write-Host "`nTrien khai Kubernetes hoan tat!" -ForegroundColor Green
        kubectl get pods,services,hpa
    }

    "status" {
        Write-Host "`n================ TRANG THAI KUBERNETES CLUSTER ================" -ForegroundColor Cyan
        Write-Host "--- PODS ---" -ForegroundColor Yellow
        kubectl get pods -o wide
        Write-Host "`n--- SERVICES ---" -ForegroundColor Yellow
        kubectl get services
        Write-Host "`n--- HORIZONTAL POD AUTOSCALER (HPA) ---" -ForegroundColor Yellow
        kubectl get hpa
    }

    "metrics-server" {
        Write-Host "`nDang cai dat Metrics Server voi co --kubelet-insecure-tls cho local k8s..." -ForegroundColor Cyan
        kubectl apply -f deployment/k8s/metrics-server.yaml
        Write-Host "Cho Metrics Server khoi dong..." -ForegroundColor Yellow
        kubectl rollout status deployment/metrics-server -n kube-system --timeout=90s
        Write-Host "Metrics Server da san sang!" -ForegroundColor Green
        kubectl top nodes
        kubectl top pods
    }

    "load-test" {
        Write-Host "`nBat dau tao tai de minh hoa HPA auto-scaling..." -ForegroundColor Yellow
        kubectl apply -f deployment/k8s/load-generator.yaml
        Write-Host "Dang ban tai! Hay theo doi scaling bang lenh:" -ForegroundColor Green
        Write-Host "kubectl get hpa -w" -ForegroundColor Cyan
    }

    "stop-load" {
        Write-Host "`nDung tao tai..." -ForegroundColor Yellow
        kubectl delete -f deployment/k8s/load-generator.yaml --ignore-not-found
        Write-Host "Tai da dung! HPA se scale down ve 1 pod sau khoang 60 giay." -ForegroundColor Green
    }

    "port-forward" {
        Write-Host "`nDang forward port ra localhost de truy cap web portal..." -ForegroundColor Cyan
        Write-Host "Frontend: http://localhost:5173" -ForegroundColor Green
        Write-Host "Backend:  http://localhost:8080" -ForegroundColor Green
        Write-Host "Nhan Ctrl+C de dung." -ForegroundColor Yellow
        kubectl port-forward svc/frontend-service 5173:80
    }

    "delete" {
        Write-Host "`nXoa toan bo tai nguyen cua he thong tren Kubernetes..." -ForegroundColor Yellow
        kubectl delete -k deployment/k8s --ignore-not-found
        kubectl delete -f deployment/k8s/load-generator.yaml --ignore-not-found
        Write-Host "Da don dep toan bo tai nguyen." -ForegroundColor Green
    }
}
