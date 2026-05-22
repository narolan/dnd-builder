// Reads the XSRF-TOKEN cookie set by Spring Security and injects it into
// every POST/PUT/DELETE fetch call so CSRF protection passes server-side.
function securedFetch(url, options = {}) {
    const raw = document.cookie.split('; ').find(c => c.startsWith('XSRF-TOKEN='));
    const token = raw ? decodeURIComponent(raw.split('=')[1]) : null;
    return fetch(url, {
        ...options,
        headers: { ...options.headers, ...(token ? { 'X-XSRF-TOKEN': token } : {}) }
    });
}

const esc = s => String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
const cap = s => s ? s.charAt(0).toUpperCase() + s.slice(1) : '';
