/* ── State ── */
let selectedCarId    = null;
let selectedDailyRate = null;

/* ── Car selection ── */
function selectCar(el) {
    document.querySelectorAll('.car-card').forEach(c => c.classList.remove('selected'));
    el.classList.add('selected');
    selectedCarId     = el.dataset.carId;
    selectedDailyRate = parseFloat(el.dataset.dailyRate);
    document.getElementById('selectedCarId').value = selectedCarId;
    updatePrice();
}

/* ── Fuel filter ── */
function filterCars(fuel, btn) {
    document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    document.querySelectorAll('.car-card').forEach(card => {
        const visible = fuel === 'Alla' || card.dataset.fuel === fuel;
        card.style.display = visible ? '' : 'none';
        if (!visible && card.classList.contains('selected')) {
            card.classList.remove('selected');
            selectedCarId = null;
            selectedDailyRate = null;
            document.getElementById('selectedCarId').value = '';
            document.getElementById('priceLabel').textContent = 'Totalt pris: –';
            document.getElementById('availabilityBadge').textContent = '';
            document.getElementById('availabilityBadge').className = 'availability-badge';
        }
    });
}

/* ── Price + availability update ── */
function updatePrice() {
    const start  = document.getElementById('startDate').value;
    const end    = document.getElementById('endDate').value;
    const priceEl = document.getElementById('priceLabel');
    const badgeEl = document.getElementById('availabilityBadge');
    const warnEl  = document.getElementById('dateWarning');

    if (!start || !end) {
        priceEl.textContent = 'Totalt pris: –';
        priceEl.style.color = '#1C4E80';
        badgeEl.textContent = '';
        badgeEl.className   = 'availability-badge';
        return;
    }

    const startDate = new Date(start);
    const endDate   = new Date(end);

    if (endDate <= startDate) {
        warnEl.style.display  = '';
        priceEl.textContent = 'Totalt pris: –';
        badgeEl.textContent = '';
        badgeEl.className   = 'availability-badge';
        updateAllAvailability(null, null);
        return;
    }
    warnEl.style.display = 'none';

    if (selectedDailyRate) {
        const days  = Math.ceil((endDate - startDate) / 86400000);
        const total = Math.round(days * selectedDailyRate);
        priceEl.textContent = `Totalt pris:  ${total} kr`;
        priceEl.style.color = '#1C4E80';

        fetch(`/api/bookings/cars/${selectedCarId}/availability?start=${start}&end=${end}`)
            .then(r => r.json())
            .then(data => {
                if (data.available) {
                    badgeEl.textContent = 'Ledig';
                    badgeEl.className   = 'availability-badge badge-available';
                } else {
                    badgeEl.textContent = 'Uppbokad';
                    badgeEl.className   = 'availability-badge badge-unavailable';
                }
            })
            .catch(() => {
                badgeEl.textContent = '';
                badgeEl.className   = 'availability-badge';
            });
    }

    updateAllAvailability(start, end);
}

/* ── Per-car availability badges in the list ── */
function updateAllAvailability(start, end) {
    document.querySelectorAll('.car-card').forEach(card => {
        const avail = document.getElementById('avail-' + card.dataset.carId);
        if (!avail) return;
        if (!start || !end) { avail.textContent = ''; avail.className = 'car-availability'; return; }

        fetch(`/api/bookings/cars/${card.dataset.carId}/availability?start=${start}&end=${end}`)
            .then(r => r.json())
            .then(data => {
                if (data.available) {
                    avail.textContent = '● Ledig';
                    avail.className   = 'car-availability avail-ok';
                } else {
                    avail.textContent = '● Uppbokad';
                    avail.className   = 'car-availability avail-busy';
                }
            })
            .catch(() => { avail.textContent = ''; avail.className = 'car-availability'; });
    });
}

/* ── Booking table filter ── */
function filterBookings(status) {
    document.querySelectorAll('.bookings-table tbody tr').forEach(row => {
        row.style.display = (status === 'Alla' || row.dataset.status === status) ? '' : 'none';
    });
}

/* ── Booking form validation ── */
function validateBooking() {
    if (!selectedCarId) {
        alert('Välj en bil från listan.');
        return false;
    }
    return true;
}

/* ── Set default dates on load ── */
(function () {
    const fmt   = d => d.toISOString().split('T')[0];
    const today = new Date();
    const tmrw  = new Date(today.getTime() + 86400000);
    const sEl   = document.getElementById('startDate');
    const eEl   = document.getElementById('endDate');
    if (sEl && !sEl.value) sEl.value = fmt(today);
    if (eEl && !eEl.value) eEl.value = fmt(tmrw);
})();
