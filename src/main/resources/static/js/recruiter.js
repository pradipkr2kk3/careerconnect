/* Recruiter dashboard */
const STATUSES = ['APPLIED', 'SHORTLISTED', 'HIRED', 'REJECTED'];

const views = {
  async dashboard() {
    const d = await api('/recruiter/dashboard');
    $('#dash').innerHTML = `
      <div class="card"><h3>${esc(d.name)}</h3><p class="muted">${esc(d.location || '')}${d.location ? ' &middot; ' : ''}${esc(d.email)}</p></div>
      <div class="stats">
        <div class="stat"><b>${d.jobs}</b>Jobs posted</div>
        <div class="stat"><b>${d.applications}</b>Applications</div>
        <div class="stat"><b>${d.shortlisted}</b>Shortlisted</div>
        <div class="stat"><b>${d.hired}</b>Hired</div>
      </div>`;
  },

  async post() { /* form only */ },

  async jobs() {
    const list = await api('/recruiter/jobs');
    $('#jobList').innerHTML = list.length ? list.map(j => `
      <div class="card item">
        <div>
          <h3>${esc(j.jobTitle)}</h3>
          <div class="muted">${esc(j.jobRole || '-')} &middot; Salary: ${money(j.salary)} &middot; Posted ${dateOnly(j.jobPostDate)}</div>
          <div class="muted">Skills: ${esc(j.skillRequired || '-')} &middot; ${j.applicants} applicant(s)</div>
        </div>
        <button class="btn danger small" onclick="deleteJob(${j.jobId})">Delete</button>
      </div>`).join('') : empty('You have not posted a job yet. Open "Post a job" to publish your first one.');
  },

  async applications() {
    const list = await api('/recruiter/applications');
    $('#appList').innerHTML = list.length ? `<div class="tablewrap"><table>
      <tr><th>Applicant</th><th>Job</th><th>Skills</th><th>Applied</th><th>Status</th></tr>
      ${list.map(a => `<tr>
        <td><b>${esc(a.userName)}</b><br><span class="muted">${esc(a.email)}${a.address ? '<br>' + esc(a.address) : ''}</span></td>
        <td>${esc(a.jobTitle)}</td>
        <td>${esc(a.skills || '-')}</td>
        <td>${dateOnly(a.applicationDate)}</td>
        <td><select aria-label="Status" onchange="setStatus(${a.applicationId}, this.value)">
          ${STATUSES.map(s => `<option ${s === a.applicationStatus ? 'selected' : ''}>${s}</option>`).join('')}
        </select></td></tr>`).join('')}
      </table></div>` : empty('No applications yet. They will appear here when job seekers apply.');
  }
};

async function deleteJob(id) {
  if (!confirm('Delete this job and its applications?')) return;
  if (await run(() => api('/recruiter/jobs/' + id, 'DELETE'), 'Job deleted')) views.jobs();
}
async function setStatus(id, status) {
  await run(() => api(`/recruiter/applications/${id}/status`, 'PUT', { status }), 'Status updated');
}

$('#jobForm').onsubmit = async e => {
  e.preventDefault();
  const ok = await run(() => api('/recruiter/jobs', 'POST', {
    jobTitle: $('#jTitle').value, jobRole: $('#jRole').value, salary: num($('#jSalary').value),
    skillRequired: $('#jSkills').value, jobDescription: $('#jDesc').value }), 'Job published');
  if (ok) { e.target.reset(); $('[data-view=jobs]').click(); }
};

guard('RECRUITER').then(me => { if (me) initNav(views, 'dashboard'); });
