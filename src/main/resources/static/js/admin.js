/* Admin dashboard */
const views = {
  async dashboard() {
    const d = await api('/admin/stats');
    $('#stats').innerHTML = `<div class="stats">
      <div class="stat"><b>${d.jobSeekers}</b>Job seekers</div>
      <div class="stat"><b>${d.recruiters}</b>Recruiters</div>
      <div class="stat"><b>${d.jobs}</b>Jobs</div>
      <div class="stat"><b>${d.applications}</b>Applications</div>
      <div class="stat"><b>${d.posts}</b>Posts</div></div>`;
  },

  async users() {
    const list = await api('/admin/users');
    $('#userList').innerHTML = list.length ? `<div class="tablewrap"><table>
      <tr><th>ID</th><th>Name</th><th>Email</th><th>Address</th><th></th></tr>
      ${list.map(u => `<tr><td>${u.userId}</td><td>${esc(u.userName)}</td><td>${esc(u.email)}</td><td>${esc(u.address || '')}</td>
        <td><button class="btn danger small" onclick="remove('users', ${u.userId}, 'users')">Remove</button></td></tr>`).join('')}
      </table></div>` : empty('No job seekers have registered yet.');
  },

  async companies() {
    const list = await api('/admin/companies');
    $('#companyList').innerHTML = list.length ? `<div class="tablewrap"><table>
      <tr><th>ID</th><th>Company</th><th>Type</th><th>Location</th><th>Email</th><th>Jobs</th><th></th></tr>
      ${list.map(c => `<tr><td>${c.companyId}</td><td>${esc(c.companyName)}</td><td>${esc(c.companyType || '')}</td>
        <td>${esc(c.companyLocation || '')}</td><td>${esc(c.companyEmail)}</td><td>${c.jobs}</td>
        <td><button class="btn danger small" onclick="remove('companies', ${c.companyId}, 'companies')">Remove</button></td></tr>`).join('')}
      </table></div>` : empty('No recruiters have registered yet.');
  }
};

async function remove(kind, id, view) {
  if (!confirm('Remove this account and everything linked to it?')) return;
  if (await run(() => api(`/admin/${kind}/${id}`, 'DELETE'), 'Removed')) views[view]();
}

guard('ADMIN').then(me => { if (me) initNav(views, 'dashboard'); });
