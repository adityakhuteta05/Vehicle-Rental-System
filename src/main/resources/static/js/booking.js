/**
 * DRIVESENSE — Interactive 3-Step Checkout & Trustworthy Live Pricing
 */

document.addEventListener('DOMContentLoaded', () => {
    let currentStep = 1;
    const form = document.getElementById('bookingForm');
    if (!form) return;

    const carId = form.getAttribute('data-car-id');
    const stepNodes = document.querySelectorAll('.step-node');
    const stepPanels = document.querySelectorAll('.step-content');

    const nextBtn = document.getElementById('btnNextStep');
    const prevBtn = document.getElementById('btnPrevStep');
    const submitBtn = document.getElementById('btnSubmitBooking');

    const startDateInput = document.getElementById('startTime');
    const endDateInput = document.getElementById('endTime');
    const driverCheck = document.getElementById('driverRequired');
    const insuranceCheck = document.getElementById('insuranceRequired');
    const childSeatCheck = document.getElementById('childSeatRequired');

    const availAlert = document.getElementById('availabilityAlert');

    function updateStep(step) {
        currentStep = step;
        stepPanels.forEach(panel => {
            panel.style.display = panel.getAttribute('data-step') === String(step) ? 'block' : 'none';
        });

        stepNodes.forEach((node, idx) => {
            const nodeStep = idx + 1;
            node.classList.remove('active', 'completed');
            if (nodeStep === currentStep) {
                node.classList.add('active');
            } else if (nodeStep < currentStep) {
                node.classList.add('completed');
            }
        });

        if (prevBtn) prevBtn.style.display = currentStep > 1 ? 'inline-flex' : 'none';
        if (nextBtn) nextBtn.style.display = currentStep < 3 ? 'inline-flex' : 'none';
        if (submitBtn) submitBtn.style.display = currentStep === 3 ? 'inline-flex' : 'none';
    }

    if (nextBtn) {
        nextBtn.addEventListener('click', () => {
            if (currentStep === 1) {
                if (!startDateInput.value || !endDateInput.value) {
                    showToast('Please select both pickup and return date-time.', 'warning');
                    return;
                }
                const start = new Date(startDateInput.value);
                const end = new Date(endDateInput.value);
                if (end <= start) {
                    showToast('Return time must be after pickup time.', 'warning');
                    return;
                }
            }
            if (currentStep < 3) updateStep(currentStep + 1);
        });
    }

    if (prevBtn) {
        prevBtn.addEventListener('click', () => {
            if (currentStep > 1) updateStep(currentStep - 1);
        });
    }

    // Live Availability Check
    async function checkAvailability() {
        if (!carId || !startDateInput.value || !endDateInput.value) return;
        const start = startDateInput.value;
        const end = endDateInput.value;
        if (new Date(end) <= new Date(start)) return;

        try {
            const resp = await fetch(`/api/cars/${carId}/availability?start=${encodeURIComponent(start)}&end=${encodeURIComponent(end)}`);
            const data = await resp.json();
            if (availAlert) {
                availAlert.style.display = 'block';
                if (data.available) {
                    availAlert.style.backgroundColor = 'var(--status-success-bg)';
                    availAlert.style.color = 'var(--status-success-text)';
                    availAlert.style.border = '1px solid var(--status-success-border)';
                    availAlert.innerHTML = '<i class="fa-solid fa-check"></i> ' + data.message;
                    if (nextBtn) nextBtn.disabled = false;
                } else {
                    availAlert.style.backgroundColor = 'var(--status-danger-bg)';
                    availAlert.style.color = 'var(--status-danger-text)';
                    availAlert.style.border = '1px solid var(--status-danger-border)';
                    availAlert.innerHTML = '<i class="fa-solid fa-xmark"></i> ' + data.message;
                    if (nextBtn) nextBtn.disabled = true;
                }
            }
        } catch (err) {
            console.error('Availability check error', err);
        }
    }

    // Live Price Breakdown Preview
    async function updatePricePreview() {
        if (!carId || !startDateInput.value || !endDateInput.value) return;
        const payload = {
            carId: parseInt(carId),
            startTime: startDateInput.value,
            endTime: endDateInput.value,
            driverRequired: driverCheck ? driverCheck.checked : false,
            insuranceRequired: insuranceCheck ? insuranceCheck.checked : true,
            childSeatRequired: childSeatCheck ? childSeatCheck.checked : false
        };

        try {
            const resp = await fetch('/api/price/preview', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (resp.ok) {
                const data = await resp.json();
                renderPriceBreakdown(data);
            }
        } catch (err) {
            console.error('Price preview error', err);
        }
    }

    function renderPriceBreakdown(data) {
        const baseEl = document.getElementById('previewBaseAmount');
        const gstEl = document.getElementById('previewGst');
        const totalEl = document.getElementById('previewTotal');
        const depositEl = document.getElementById('previewDeposit');
        const co2El = document.getElementById('previewCo2');

        if (baseEl) baseEl.innerText = `₹${Math.round(data.baseAmount).toLocaleString()}`;
        if (gstEl) gstEl.innerText = `₹${Math.round(data.gstAmount).toLocaleString()}`;
        if (totalEl) totalEl.innerText = `₹${Math.round(data.totalAmount).toLocaleString()}`;
        if (depositEl) depositEl.innerText = data.depositAmount === 0 ? '₹0 (Waived)' : `₹${Math.round(data.depositAmount).toLocaleString()}`;
        if (co2El) co2El.innerText = `${data.estCo2Kg} kg CO₂`;
    }

    // Attach listeners
    [startDateInput, endDateInput].forEach(inp => {
        if (inp) {
            inp.addEventListener('change', () => {
                checkAvailability();
                updatePricePreview();
            });
        }
    });

    [driverCheck, insuranceCheck, childSeatCheck].forEach(chk => {
        if (chk) {
            chk.addEventListener('change', updatePricePreview);
        }
    });

    // Form submission simulated loader
    form.addEventListener('submit', () => {
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.innerHTML = 'Securing Reservation...';
        }
    });
});
