/* Shared helpers used by every page */
const $ = (s, r = document) => r.querySelector(s);
const $$ = (s, r = document) => [...r.querySelectorAll(s)];
const esc = v => String(v ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const dateOnly = v => (v ? String(v).slice(0, 10) : '');
const money = v => (v == null || v === '' ? 'Not disclosed' : Number(v).toLocaleString('en-IN'));
const num = v => (v === '' || v == null ? null : Number(v));
const val = v => (v === '' ? null : v);

async function api(path, method = 'GET', body) {
  const res = await fetch('/api' + path, {
    method,
    headers: { 'Content-Type': 'application/json' },
    body: body === undefined ? undefined : JSON.stringify(body)
  });
  let data = null;
  try { data = await res.json(); } catch (e) { /* no body */ }
  if (!res.ok) {
    if (res.status === 401 && !/login|register|index|\/$/.test(location.pathname)) location.href = '/login.html';
    throw new Error((data && data.message) || 'Something went wrong. Please try again.');
  }
  return data;
}

let toastTimer;
function toast(msg, bad) {
  let t = $('#toast');
  if (!t) { t = document.createElement('div'); t.id = 'toast'; t.setAttribute('role', 'status'); document.body.appendChild(t); }
  t.textContent = msg;
  t.className = 'show' + (bad ? ' bad' : '');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => (t.className = ''), 2800);
}

/** Run an async action and show success / error toast. */
async function run(fn, okMsg) {
  try { const r = await fn(); if (okMsg) toast(okMsg); return r; }
  catch (e) { toast(e.message, true); return undefined; }
}

async function guard(role) {
  try {
    const me = await api('/auth/me');
    if (me.role !== role) { location.href = '/login.html'; return null; }
    const w = $('#who'); if (w) w.textContent = me.name;
    return me;
  } catch (e) { location.href = '/login.html'; return null; }
}

async function logout() {
  await run(() => api('/auth/logout', 'POST'));
  location.href = '/';
}

/** Sidebar navigation: buttons carry data-view="x" and views = { x: loaderFunction } */
function initNav(views, first) {
  const show = name => {
    $$('.side button').forEach(b => b.classList.toggle('on', b.dataset.view === name));
    $$('.view').forEach(v => v.classList.toggle('on', v.id === 'view-' + name));
    location.hash = name;
    if (views[name]) views[name]();
  };
  $$('.side button').forEach(b => (b.onclick = () => show(b.dataset.view)));
  const start = location.hash.slice(1);
  show(views[start] ? start : first);
}

const empty = text => `<div class="empty">${esc(text)}</div>`;
const chip = s => `<span class="chip ${esc(s)}">${esc(s)}</span>`;
