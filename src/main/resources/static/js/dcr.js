/**
 * DriveSense - Digital Condition Report (DCR) Interactive Wireframe & Fuel Gauge
 */

document.addEventListener('DOMContentLoaded', () => {
    // 1. Interactive Car Damage Zone Selector
    const zoneButtons = document.querySelectorAll('.zone-btn');
    const hiddenDamageInput = document.getElementById('damageZonesInput');

    let selectedZones = new Set();
    if (hiddenDamageInput && hiddenDamageInput.value) {
        hiddenDamageInput.value.split(',').forEach(z => {
            if (z.trim()) selectedZones.add(z.trim());
        });
    }

    zoneButtons.forEach(btn => {
        const zone = btn.getAttribute('data-zone');
        if (selectedZones.has(zone)) {
            btn.classList.add('damaged');
        }

        btn.addEventListener('click', (e) => {
            e.preventDefault();
            if (selectedZones.has(zone)) {
                selectedZones.delete(zone);
                btn.classList.remove('damaged');
            } else {
                selectedZones.add(zone);
                btn.classList.add('damaged');
            }

            if (hiddenDamageInput) {
                hiddenDamageInput.value = Array.from(selectedZones).join(',');
            }
        });
    });

    // 2. Fuel Range Slider
    const fuelSlider = document.getElementById('fuelPercentSlider');
    const fuelDisplay = document.getElementById('fuelPercentDisplay');

    if (fuelSlider && fuelDisplay) {
        fuelSlider.addEventListener('input', () => {
            fuelDisplay.innerText = `${fuelSlider.value}%`;
            if (fuelSlider.value < 25) {
                fuelDisplay.style.color = '#FF5C5C';
            } else if (fuelSlider.value < 60) {
                fuelDisplay.style.color = '#F39C12';
            } else {
                fuelDisplay.style.color = '#2ECC71';
            }
        });
    }
});
