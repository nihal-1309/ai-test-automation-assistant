async function postJson(url, body){
  const res = await fetch(url, {method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(body)});
  return res.ok ? res.json() : {error: await res.text()};
}

document.getElementById('gen').onclick = async ()=>{
  const val = document.getElementById('input').value;
  const out = document.getElementById('out');
  out.textContent = 'Generating...';
  try{
    const resp = await fetch('/api/generate-testcases',{method:'POST',headers:{'Content-Type':'text/plain'},body:val});
    const json = await resp.json();
    out.textContent = JSON.stringify(json,null,2);
  }catch(e){ out.textContent = ''+e }
}

document.getElementById('run').onclick = async ()=>{
  const val = document.getElementById('input').value;
  const out = document.getElementById('out');
  out.textContent = 'Generating and running...';
  try{
    const resp = await fetch('/api/generate-testcases',{method:'POST',headers:{'Content-Type':'text/plain'},body:val});
    const cases = await resp.json();
    const runResp = await postJson('/api/run-tests', cases);
    out.textContent = JSON.stringify(runResp,null,2);
  }catch(e){ out.textContent = ''+e }
}

document.getElementById('report').onclick = async ()=>{
  const out = document.getElementById('out');
  try{
    const resp = await fetch('/api/report');
    const p = await resp.text();
    if (!p) out.textContent = 'No report yet';
    else window.open(p,'_blank');
  }catch(e){ out.textContent = ''+e }
}
