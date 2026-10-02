$base = 'http://localhost:8080'

# 1. Availability
$r1 = Invoke-RestMethod -Uri "$base/api/cars/1/availability?startDate=2026-10-15T10:00:00&endDate=2026-10-18T10:00:00"
Write-Host "Availability API response: $($r1 | ConvertTo-Json -Compress)"

# 2. Price preview
$body = @{
    carId = 1
    startTime = '2026-10-15T10:00:00'
    endTime = '2026-10-18T10:00:00'
    insurancePlan = 'STANDARD'
    extraKms = 0
} | ConvertTo-Json
$r2 = Invoke-RestMethod -Uri "$base/api/price/preview" -Method POST -Body $body -ContentType 'application/json'
Write-Host "Price Preview API response: Total = ₹$($r2.totalAmount), Deposit = ₹$($r2.depositAmount)"

# 3. Booked dates
$r3 = Invoke-RestMethod -Uri "$base/api/cars/1/booked-dates"
Write-Host "Booked Dates API response count: $($r3.Count)"

# 4. Admin stats revenue
$r4 = Invoke-RestMethod -Uri "$base/api/admin/stats/revenue"
Write-Host "Revenue API labels: $($r4.labels -join ', ')"
