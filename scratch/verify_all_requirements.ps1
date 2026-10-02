$base = 'http://localhost:8080'

Write-Host "============================================="
Write-Host "DRIVESENSE FRONTEND VERIFICATION SUITE"
Write-Host "============================================="

$testsPassed = 0
$totalTests = 0

function Assert-Contains($content, $pattern, $testName) {
    $global:totalTests++
    if ($content -match $pattern) {
        Write-Host " [PASS] $testName" -ForegroundColor Green
        $global:testsPassed++
    } else {
        Write-Host " [FAIL] $testName - Missing pattern: $pattern" -ForegroundColor Red
    }
}

function Get-AuthenticatedSession($email, $password) {
    $session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $loginPage = (Invoke-WebRequest -Uri "$base/login" -SessionVariable session -UseBasicParsing).Content
    $csrf = ""
    if ($loginPage -match 'name="_csrf"\s+value="([^"]+)"') {
        $csrf = $matches[1]
    }
    
    $postBody = @{
        username = $email
        password = $password
        _csrf = $csrf
    }
    
    $loginResp = Invoke-WebRequest -Uri "$base/login" -Method POST -Body $postBody -WebSession $session -UseBasicParsing -MaximumRedirection 5
    return $session
}

# 1. Homepage Checks
$homeContent = (Invoke-WebRequest -Uri "$base/" -UseBasicParsing).Content
Assert-Contains $homeContent 'DRIVESENSE' "Homepage Branding: DRIVESENSE Wordmark"
Assert-Contains $homeContent 'Drive your way\.' "Requirement 8: Hero Headline 'Drive your way.'"
Assert-Contains $homeContent 'Premium cars\.\s+Transparent pricing\.\s+A simpler way to rent\.' "Requirement 8: Hero Supporting Text"
Assert-Contains $homeContent 'EXPLORE CARS' "Requirement 8: CTA EXPLORE CARS"
Assert-Contains $homeContent 'FIND YOUR MATCH' "Requirement 8: Secondary CTA FIND YOUR MATCH"
Assert-Contains $homeContent 'Choose your journey\.' "Requirement 10: Section Title 'Choose your journey.'"
Assert-Contains $homeContent 'FAMILY' "Requirement 10: Journey Type FAMILY"
Assert-Contains $homeContent 'WEDDING' "Requirement 10: Journey Type WEDDING"
Assert-Contains $homeContent 'WEEKEND' "Requirement 10: Journey Type WEEKEND"
Assert-Contains $homeContent 'BUSINESS' "Requirement 10: Journey Type BUSINESS"
Assert-Contains $homeContent 'HILLS' "Requirement 10: Journey Type HILLS"
Assert-Contains $homeContent 'AIRPORT' "Requirement 10: Journey Type AIRPORT"
Assert-Contains $homeContent 'Selected for you\.' "Requirement 11: Section Title 'Selected for you.'"
Assert-Contains $homeContent 'Pickup Location' "Requirement 9: Search Field Pickup Location"
Assert-Contains $homeContent 'SEARCH CARS' "Requirement 9: Search Button"

# Check color system and card design in CSS
$css = (Invoke-WebRequest -Uri "$base/css/style.css" -UseBasicParsing).Content
Assert-Contains $css '#0B0D0F' "Color System: Primary Background #0B0D0F"
Assert-Contains $css '#121518' "Color System: Secondary Background #121518"
Assert-Contains $css '#171A1D' "Color System: Card Background #171A1D"
Assert-Contains $css '#F5F5F3' "Color System: Primary Text #F5F5F3"
Assert-Contains $css '#A7AAAD' "Color System: Secondary Text #A7AAAD"
Assert-Contains $css '#73777B' "Color System: Muted Text #73777B"
Assert-Contains $css '12px' "Requirement 32: Buttons 12px radius"
Assert-Contains $css 'scale\(1\.02\)' "Requirement 12: Image zoom 1.02"
Assert-Contains $css 'translateY\(-3px\)' "Requirement 12: Card movement 2-4px"

# 2. Catalogue Page Checks
$cars = (Invoke-WebRequest -Uri "$base/cars" -UseBasicParsing).Content
Assert-Contains $cars 'Find your next drive\.' "Requirement 13: Catalogue Heading"
Assert-Contains $cars 'Browse vehicles by style, capacity and price\.' "Requirement 13: Catalogue Subheading"
Assert-Contains $cars 'Class|Vehicle' "Requirement 13: Filter Vehicle Type"
Assert-Contains $cars 'Fuel|Powertrain' "Requirement 13: Filter Fuel"
Assert-Contains $cars 'Transmission' "Requirement 13: Filter Transmission"
Assert-Contains $cars 'Seats|Seating' "Requirement 13: Filter Seats"

# 3. Vehicle Details Page Checks
$carDetails = (Invoke-WebRequest -Uri "$base/cars/1" -UseBasicParsing).Content
Assert-Contains $carDetails 'Designed for comfortable everyday journeys\.' "Requirement 14: Car Description"
Assert-Contains $carDetails 'BOOK NOW' "Requirement 14: Sticky Booking Panel CTA"
Assert-Contains $carDetails 'Specifications' "Requirement 14: Specifications Section"
Assert-Contains $carDetails 'Features' "Requirement 14: Features Section"

# 4. Vibe Match Checks
$vibe = (Invoke-WebRequest -Uri "$base/vibe-match" -UseBasicParsing).Content
Assert-Contains $vibe 'Tell us about your journey\.' "Requirement 16: Vibe Match Heading"
Assert-Contains $vibe '01' "Requirement 16: Step 01"
Assert-Contains $vibe '02' "Requirement 16: Step 02"
Assert-Contains $vibe '03' "Requirement 16: Step 03"
Assert-Contains $vibe '04' "Requirement 16: Step 04"

# 5. Customer Session (user@example.com / user123)
$renterSession = Get-AuthenticatedSession 'user@example.com' 'user123'

$renterDash = (Invoke-WebRequest -Uri "$base/renter/dashboard" -WebSession $renterSession -UseBasicParsing).Content
Assert-Contains $renterDash 'Good morning' "Requirement 19: Customer Dashboard Heading"
Assert-Contains $renterDash 'NEXT JOURNEY' "Requirement 19: Main Section NEXT JOURNEY"
Assert-Contains $renterDash 'VIEW BOOKING' "Requirement 19: Button VIEW BOOKING"

$renterProfile = (Invoke-WebRequest -Uri "$base/renter/profile" -WebSession $renterSession -UseBasicParsing).Content
Assert-Contains $renterProfile 'TRUST SCORE' "Requirement 20: Trust Score Title"
Assert-Contains $renterProfile 'GOLD' "Requirement 20: Trust Tier GOLD"
Assert-Contains $renterProfile 'On-time return' "Requirement 20: On-time return rule"
Assert-Contains $renterProfile '5-star review' "Requirement 20: 5-star review rule"
Assert-Contains $renterProfile 'Late return' "Requirement 20: Late return rule"
Assert-Contains $renterProfile 'New damage' "Requirement 20: New damage rule"

$renterBookings = (Invoke-WebRequest -Uri "$base/renter/bookings" -WebSession $renterSession -UseBasicParsing).Content
Assert-Contains $renterBookings 'My journeys\.' "Requirement 21: My Bookings Heading"
Assert-Contains $renterBookings 'Upcoming' "Requirement 21: Tab Upcoming"
Assert-Contains $renterBookings 'Active' "Requirement 21: Tab Active"
Assert-Contains $renterBookings 'Completed' "Requirement 21: Tab Completed"
Assert-Contains $renterBookings 'Cancelled' "Requirement 21: Tab Cancelled"

$invoice = (Invoke-WebRequest -Uri "$base/bookings/1/invoice" -WebSession $renterSession -UseBasicParsing).Content
Assert-Contains $invoice 'DriveSense' "Requirement 24: Invoice Logo/Name"
Assert-Contains $invoice 'DS-2026-000123' "Requirement 24: Invoice Number"
Assert-Contains $invoice 'Print' "Requirement 24: Invoice Print Button"
Assert-Contains $invoice 'Download XML' "Requirement 24: Download XML Button"

# 6. Admin Session (admin@drivesense.com / admin123)
$adminSession = Get-AuthenticatedSession 'admin@drivesense.com' 'admin123'

$adminXml = (Invoke-WebRequest -Uri "$base/admin/fleet/import-xml" -WebSession $adminSession -UseBasicParsing).Content
Assert-Contains $adminXml 'Import Fleet' "Requirement 30: XML Import Title"
Assert-Contains $adminXml 'Upload a validated XML fleet file to add vehicles in bulk\.' "Requirement 30: XML Import Description"
Assert-Contains $adminXml 'Drag &amp; drop XML file' "Requirement 30: XML Dropzone text"
Assert-Contains $adminXml 'Choose file' "Requirement 30: Choose file button"

$adminRep = (Invoke-WebRequest -Uri "$base/admin/reports" -WebSession $adminSession -UseBasicParsing).Content
Assert-Contains $adminRep 'Monthly Revenue' "Requirement 29: Revenue Dashboard Chart Title"
Assert-Contains $adminRep 'Revenue by Vehicle Type' "Requirement 29: Revenue by Vehicle Type"
Assert-Contains $adminRep 'Fleet Utilization' "Requirement 29: Fleet Utilization"
Assert-Contains $adminRep 'Booking Volume' "Requirement 29: Booking Volume"

# 7. Owner Session (owner@drivesense.com / owner123)
$ownerSession = Get-AuthenticatedSession 'owner@drivesense.com' 'owner123'

$ownerDash = (Invoke-WebRequest -Uri "$base/owner/dashboard" -WebSession $ownerSession -UseBasicParsing).Content
Assert-Contains $ownerDash 'Fleet Overview' "Requirement 25: Owner Dashboard Main Title"
Assert-Contains $ownerDash 'Total Vehicles' "Requirement 25: Stat Total Vehicles"
Assert-Contains $ownerDash 'Currently Rented' "Requirement 25: Stat Currently Rented"
Assert-Contains $ownerDash 'Available' "Requirement 25: Stat Available"
Assert-Contains $ownerDash 'Due Today' "Requirement 25: Stat Due Today"
Assert-Contains $ownerDash 'Monthly Revenue' "Requirement 25: Stat Monthly Revenue"

$ownerBookings = (Invoke-WebRequest -Uri "$base/owner/bookings" -WebSession $ownerSession -UseBasicParsing).Content
Assert-Contains $ownerBookings 'Booking Management' "Requirement 27: Heading Booking Management"
Assert-Contains $ownerBookings 'Start Trip' "Requirement 27: Action Start Trip"

Write-Host "============================================="
Write-Host "FINAL RESULTS: $testsPassed / $totalTests TESTS PASSED"
Write-Host "============================================="
