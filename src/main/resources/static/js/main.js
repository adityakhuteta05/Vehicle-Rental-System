/**
 * DRIVESENSE — Luxury Automotive Client Experience & Theme Engine
 */

document.addEventListener('DOMContentLoaded', () => {
    // 1. Theme Management (Light Showroom / Dark Cockpit)
    const storedTheme = localStorage.getItem('drivesense-theme') || 'light';
    document.documentElement.setAttribute('data-theme', storedTheme);

    const themeToggleBtns = document.querySelectorAll('.theme-toggle-btn');
    themeToggleBtns.forEach(btn => {
        updateThemeIcon(btn, storedTheme);
        btn.addEventListener('click', () => {
            const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
            const newTheme = currentTheme === 'light' ? 'dark' : 'light';
            document.documentElement.setAttribute('data-theme', newTheme);
            localStorage.setItem('drivesense-theme', newTheme);
            themeToggleBtns.forEach(b => updateThemeIcon(b, newTheme));
            
            // Dispatch custom event for Chart.js updates if present
            window.dispatchEvent(new CustomEvent('drivesense-theme-changed', { detail: { theme: newTheme } }));
        });
    });

    function updateThemeIcon(btn, theme) {
        if (!btn) return;
        const icon = btn.querySelector('i');
        if (icon) {
            if (theme === 'dark') {
                icon.className = 'fa-regular fa-sun';
                btn.setAttribute('title', 'Switch to Showroom Light Mode');
                btn.setAttribute('aria-label', 'Switch to Showroom Light Mode');
            } else {
                icon.className = 'fa-regular fa-moon';
                btn.setAttribute('title', 'Switch to Cockpit Dark Mode');
                btn.setAttribute('aria-label', 'Switch to Cockpit Dark Mode');
            }
        }
    }

    // 2. Mobile Drawer Navigation Toggle
    const mobileToggle = document.querySelector('.mobile-nav-toggle');
    const mobileDrawer = document.querySelector('.mobile-drawer');
    if (mobileToggle && mobileDrawer) {
        mobileToggle.addEventListener('click', () => {
            mobileDrawer.classList.toggle('open');
            const icon = mobileToggle.querySelector('i');
            if (icon) {
                if (mobileDrawer.classList.contains('open')) {
                    icon.className = 'fa-solid fa-xmark';
                } else {
                    icon.className = 'fa-solid fa-bars';
                }
            }
        });
    }

    // 3. Viewport Metric Counters (IntersectionObserver)
    const counters = document.querySelectorAll('.counter-val');
    if (counters.length > 0 && 'IntersectionObserver' in window) {
        const counterObserver = new IntersectionObserver((entries, observer) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    const counter = entry.target;
                    const target = parseInt(counter.getAttribute('data-target'), 10);
                    if (!isNaN(target)) {
                        let count = 0;
                        const duration = 1000;
                        const stepTime = 20;
                        const totalSteps = duration / stepTime;
                        const increment = target / totalSteps;

                        const timer = setInterval(() => {
                            count += increment;
                            if (count >= target) {
                                counter.innerText = target.toLocaleString();
                                clearInterval(timer);
                            } else {
                                counter.innerText = Math.floor(count).toLocaleString();
                            }
                        }, stepTime);
                    }
                    observer.unobserve(counter);
                }
            });
        }, { threshold: 0.25 });

        counters.forEach(counter => counterObserver.observe(counter));
    }

    // 4. Search Bar Live Availability Feedback
    const pickupDate = document.getElementById('searchPickup');
    const returnDate = document.getElementById('searchReturn');
    const searchStatusText = document.getElementById('searchLiveStatus');

    function checkSearchAvailability() {
        if (!searchStatusText) return;
        if (pickupDate && returnDate && pickupDate.value && returnDate.value) {
            searchStatusText.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Checking verified fleet availability...';
            setTimeout(() => {
                searchStatusText.innerHTML = '<i class="fa-solid fa-circle-check" style="color: var(--status-success-text);"></i> <strong>13 verified vehicles available</strong> with guaranteed zero double-bookings.';
            }, 300);
        }
    }

    if (pickupDate) pickupDate.addEventListener('change', checkSearchAvailability);
    if (returnDate) returnDate.addEventListener('change', checkSearchAvailability);

    // 5. Default Minimum Date-Time configuration
    const nowIso = new Date().toISOString().slice(0, 16);
    document.querySelectorAll('input[type="datetime-local"], input[type="date"]').forEach(input => {
        if (!input.value && !input.min) {
            input.min = nowIso.slice(0, 10);
        }
    });
});

/**
 * Monochromatic Toast Notification Engine
 */
function showToast(message, type = 'info') {
    let container = document.getElementById('drivesense-toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'drivesense-toast-container';
        container.style.position = 'fixed';
        container.style.bottom = '24px';
        container.style.right = '24px';
        container.style.zIndex = '9999';
        container.style.display = 'flex';
        container.style.flexDirection = 'column';
        container.style.gap = '0.65rem';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = 'drivesense-toast';
    toast.style.background = 'var(--bg-surface)';
    toast.style.border = '1px solid var(--border-strong)';
    toast.style.boxShadow = 'var(--shadow-lg)';
    toast.style.padding = '0.9rem 1.4rem';
    toast.style.fontSize = '0.88rem';
    toast.style.fontWeight = '500';
    toast.style.color = 'var(--text-primary)';
    toast.style.borderRadius = '12px';
    toast.style.display = 'flex';
    toast.style.alignItems = 'center';
    toast.style.gap = '0.75rem';
    toast.style.transition = 'all 0.25s ease';

    let icon = 'fa-info';
    if (type === 'success') icon = 'fa-check';
    if (type === 'warning') icon = 'fa-triangle-exclamation';
    if (type === 'danger') icon = 'fa-xmark';

    toast.innerHTML = `<i class="fa-solid ${icon}"></i> <span>${message}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(10px)';
        setTimeout(() => toast.remove(), 260);
    }, 3500);
}
