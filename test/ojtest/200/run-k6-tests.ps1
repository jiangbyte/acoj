$testScript = Join-Path $PSScriptRoot "optimized-oj-test.js"
$iterations = 10
$interval = 10

Write-Host "开始运行k6测试脚本，共 $iterations 次，每次间隔 $interval 秒" -ForegroundColor Green
Write-Host "测试脚本: $testScript" -ForegroundColor Cyan
Write-Host ""

for ($i = 1; $i -le $iterations; $i++) {
    Write-Host "========================================" -ForegroundColor Yellow
    Write-Host "第 $i/$iterations 次测试开始" -ForegroundColor Yellow
    Write-Host "时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Gray
    Write-Host "========================================" -ForegroundColor Yellow
    
    k6 run $testScript
    
    Write-Host ""
    
    if ($i -lt $iterations) {
        Write-Host "等待 $interval 秒后进行下一次测试..." -ForegroundColor Gray
        Start-Sleep -Seconds $interval
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "所有测试完成！共运行 $iterations 次" -ForegroundColor Green
Write-Host "完成时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
