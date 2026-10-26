/* Job seeker dashboard */
const views = {

  async dashboard() {
    const d = await api('/seeker/dashboard');
    $('#dash').innerHTML = `
      <div class="card"><h3>${esc(d.name)}</h3><p class="muted">${esc(d.email)}${d.address ? ' &middot; ' + esc(d.address) : ''}</p></div>
      <div class="stats">
        <div class="stat"><b>${d.applications}</b>Applications</div>
        <div class="stat"><b>${d.skills}</b>Skills</div>
        <div class="stat"><b>${d.education}</b>Education</div>
        <div class="stat"><b>${d.experience}</b>Experience</div>
        <div class="stat"><b>${d.posts}</b>Posts</div>
      </div>
      <p class="muted">Complete your education, skills and experience so recruiters see a full profile when you apply.</p>`;
  },

  async jobs() {
    const list = await run(() => api('/seeker/jobs?q=' + encodeURIComponent($('#q').value)));
    if (!list) return;
    $('#jobList').innerHTML = list.length ? list.map(j => `
      <div class="card"><div class="item">
        <div>
          <h3>${esc(j.jobTitle)}</h3>
          <div class="muted">${esc(j.companyName)}${j.companyLocation ? ' &middot; ' + esc(j.companyLocation) : ''} &middot; posted ${dateOnly(j.jobPostDate)}</div>
          <p>${esc(j.jobDescription)}</p>
          <div class="muted">Role: ${esc(j.jobRole || '-')} &middot; Salary: ${money(j.salary)} &middot; Skills: ${esc(j.skillRequired || '-')}</div>
        </div>
        <button class="btn" ${j.applied ? 'disabled' : ''} onclick="applyJob(${j.jobId})">${j.applied ? 'Applied' : 'Apply'}</button>
      </div></div>`).join('') : empty('No jobs match your search yet.');
  },

  async applications() {
    const list = await api('/seeker/applications');
    $('#appList').innerHTML = list.length ? `<div class="tablewrap"><table>
      <tr><th>Job</th><th>Company</th><th>Applied on</th><th>Status</th></tr>
      ${list.map(a => `<tr><td>${esc(a.jobTitle)}</td><td>${esc(a.companyName)}</td><td>${dateOnly(a.applicationDate)}</td><td>${chip(a.applicationStatus)}</td></tr>`).join('')}
      </table></div>` : empty('You have not applied to any job yet. Open "Find jobs" to start.');
  },

  async education() {
    const list = await api('/seeker/education');
    $('#eduList').innerHTML = list.length ? list.map(e => `
      <div class="card tight item">
        <div><h3>${esc(e.instituteName)}</h3>
          <div class="muted">${esc(e.degree || '')} &middot; ${e.startYear || '?'} - ${e.endYear || 'present'}${e.grade ? ' &middot; ' + esc(e.grade) : ''}</div></div>
        <button class="btn danger small" onclick="del('/seeker/education/${e.educationId}', 'education')">Delete</button>
      </div>`).join('') : empty('No education added yet.');
  },

  async skills() {
    const [catalog, mine] = await Promise.all([api('/seeker/skills'), api('/seeker/my-skills')]);
    $('#sSkill').innerHTML = catalog.map(s => `<option value="${s.skillId}">${esc(s.skillName)}</option>`).join('');
    $('#skillList').innerHTML = mine.length ? mine.map(s => `
      <div class="card tight item">
        <div><h3>${esc(s.skillName)}</h3>
          <div class="muted">${esc(s.skillCategory || '')} &middot; ${esc(s.proficiencyLevel || '')} &middot; ${s.yearOfExperience ?? 0} yr</div></div>
        <button class="btn danger small" onclick="del('/seeker/my-skills/${s.skillId}', 'skills')">Remove</button>
      </div>`).join('') : empty('No skills added yet.');
  },

  async experience() {
    const [companies, list] = await Promise.all([api('/seeker/companies'), api('/seeker/experience')]);
    $('#xCompany').innerHTML = '<option value="">- not listed -</option>' +
      companies.map(c => `<option value="${c.companyId}">${esc(c.companyName)}</option>`).join('');
    $('#expList').innerHTML = list.length ? list.map(x => `
      <div class="card tight item">
        <div><h3>${esc(x.designation)} at ${esc(x.companyName)}</h3>
          <div class="muted">${dateOnly(x.joiningDate) || '?'} to ${dateOnly(x.endDate) || 'present'}</div>
          ${x.roleDescription ? `<p>${esc(x.roleDescription)}</p>` : ''}</div>
        <button class="btn danger small" onclick="del('/seeker/experience/${x.experienceId}', 'experience')">Delete</button>
      </div>`).join('') : empty('No experience added yet.');
  },

  async feed() {
    const list = await api('/feed');
    $('#posts').innerHTML = list.length ? list.map(p => `
      <div class="card post" id="post-${p.postId}">
        <div class="meta"><b>${esc(p.userName)}</b><span>${dateOnly(p.postDate)}${p.location ? ' &middot; ' + esc(p.location) : ''}</span></div>
        <div class="body">${esc(p.postContent)}</div>
        <div class="actions">
          <button class="btn ghost small ${p.likedByMe ? 'liked' : ''}" onclick="likePost(${p.postId})">${p.likedByMe ? 'Liked' : 'Like'} (${p.likes})</button>
          <button class="btn ghost small" onclick="toggleComments(${p.postId})">Comments (${p.comments})</button>
          <button class="btn ghost small" onclick="sharePost(${p.postId})">Share (${p.shares})</button>
        </div>
        <div class="comments" id="c-${p.postId}" hidden></div>
      </div>`).join('') : empty('No posts yet. Be the first to share an update.');
  }
};

/* ---------- actions ---------- */
async function applyJob(id) {
  if (await run(() => api(`/seeker/jobs/${id}/apply`, 'POST'), 'Application sent')) views.jobs();
}
async function del(path, view) {
  if (!confirm('Delete this item?')) return;
  if (await run(() => api(path, 'DELETE'), 'Deleted')) views[view]();
}
async function likePost(id) { if (await run(() => api(`/feed/${id}/like`, 'POST'))) views.feed(); }
async function sharePost(id) { if (await run(() => api(`/feed/${id}/share`, 'POST', {}), 'Post shared')) views.feed(); }

async function toggleComments(id) {
  const box = $('#c-' + id);
  if (!box.hidden) { box.hidden = true; return; }
  const list = await run(() => api(`/feed/${id}/comments`));
  if (!list) return;
  box.innerHTML = list.map(c => `<div class="comment"><b>${esc(c.userName)}</b>${esc(c.commentContent)}</div>`).join('') +
    `<form class="searchbar" onsubmit="addComment(event, ${id})"><input placeholder="Write a comment" required aria-label="Comment"><button class="btn small" type="submit">Comment</button></form>`;
  box.hidden = false;
}
async function addComment(e, id) {
  e.preventDefault();
  const text = e.target.querySelector('input').value;
  if (await run(() => api(`/feed/${id}/comments`, 'POST', { commentContent: text }), 'Comment added')) {
    await views.feed();
    toggleComments(id);
  }
}

/* ---------- forms ---------- */
$('#jobSearch').onsubmit = e => { e.preventDefault(); views.jobs(); };

$('#eduForm').onsubmit = async e => {
  e.preventDefault();
  const ok = await run(() => api('/seeker/education', 'POST', {
    instituteName: $('#eInst').value, degree: $('#eDeg').value,
    startYear: num($('#eStart').value), endYear: num($('#eEnd').value), grade: $('#eGrade').value }), 'Education added');
  if (ok) { e.target.reset(); views.education(); }
};

$('#skillForm').onsubmit = async e => {
  e.preventDefault();
  const ok = await run(() => api('/seeker/my-skills', 'POST', {
    skillId: Number($('#sSkill').value), proficiencyLevel: $('#sLevel').value, yearOfExperience: num($('#sYears').value) }), 'Skill saved');
  if (ok) views.skills();
};

$('#newSkillForm').onsubmit = async e => {
  e.preventDefault();
  const ok = await run(() => api('/seeker/skills', 'POST', { skillName: $('#nsName').value, skillCategory: $('#nsCat').value }), 'Skill added to the list');
  if (ok) { e.target.reset(); views.skills(); }
};

$('#expForm').onsubmit = async e => {
  e.preventDefault();
  const companyId = num($('#xCompany').value);
  const ok = await run(() => api('/seeker/experience', 'POST', {
    companyId, companyName: companyId ? null : $('#xCompanyName').value,
    designation: $('#xDesig').value, roleDescription: $('#xDesc').value,
    joiningDate: val($('#xFrom').value), endDate: val($('#xTo').value) }), 'Experience added');
  if (ok) { e.target.reset(); views.experience(); }
};

$('#postForm').onsubmit = async e => {
  e.preventDefault();
  const ok = await run(() => api('/feed', 'POST', { postContent: $('#pText').value, location: $('#pLoc').value }), 'Post published');
  if (ok) { e.target.reset(); views.feed(); }
};

guard('JOB_SEEKER').then(me => { if (me) initNav(views, 'dashboard'); });
