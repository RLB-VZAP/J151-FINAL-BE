// src/main/webapp/assets/js/search-filter.js
// Debounced auto-submit for player/club search forms (W2-T02B).
// Does NOT filter data itself — it just triggers the existing GET form
// submission after the user pauses typing, so the servlet + backend
// remain the single source of truth for search results.
(function () {
    const DEBOUNCE_MS = 400;

    function attachDebouncedSubmit(inputId) {
        const input = document.getElementById(inputId);
        if (!input) return;

        let timer = null;
        input.addEventListener('input', function () {
            clearTimeout(timer);
            timer = setTimeout(function () {
                input.form.submit();
            }, DEBOUNCE_MS);
        });
    }

    attachDebouncedSubmit('playerSearchInput');
    attachDebouncedSubmit('clubSearchInput');
})();