function applyTheme() {
    const selected = document.getElementById('themeSelector').value;
    const body = document.body;

    // 기존 클래스 초기화
    body.className = `theme-${selected}`;
    removeEffects();

    // 추가 애니메이션 요소 생성
    if (selected === 'rain') {
        createRainDrops(60);
    } else if (selected === 'snow') {
        createSnowflakes(50);
    }
}

function removeEffects() {
    document.querySelectorAll('.rain-drop, .snowflake').forEach(el => el.remove());
}

function createRainDrops(count) {
    for (let i = 0; i < count; i++) {
        const drop = document.createElement('div');
        drop.className = 'rain-drop';
        drop.style.transform = `translateY(${Math.random() * -200}vh)`;
        drop.style.left = `${Math.random() * 100}vw`;
        drop.style.animationDelay = `${Math.random()}s`;
        drop.style.height = `${10 + Math.random() * 20}px`;
        document.body.appendChild(drop);
    }
}

function createSnowflakes(count) {
    for (let i = 0; i < count; i++) {
        const flake = document.createElement('div');
        flake.className = 'snowflake';
        flake.style.transform = `translateY(${Math.random() * -200}vh)`;
        flake.style.left = `${Math.random() * 100}vw`;
        flake.style.animationDuration = `${2 + Math.random() * 5}s`;
        flake.style.opacity = `${0.3 + Math.random() * 0.7}`;
        document.body.appendChild(flake);
    }
}

async function getPokemon() {
    const id = document.getElementById('pokemonId').value;
    const result = document.getElementById('result');
    const error = document.getElementById('error');
    result.innerHTML = '';
    error.textContent = '';

    try {
        const res = await fetch(`/pokemon/${id}`);
        if (!res.ok) throw new Error();
        const data = await res.json();

        result.innerHTML = `
      <div class="card">
        <h2>${data.name} (#${data.id})</h2>
        <p>Height: ${data.height}</p>
        <div class="images">
          <img src="${data.frontImageUrl}" />
          <img src="${data.backImageUrl}" />
        </div>
        <div class="stats">
          <h3>Stats</h3>
          ${data.stats.map(stat => `
            <div>
              <div class="stat-label">${stat.name}</div>
              <div class="stat-bar">
                <span style="width: ${stat.baseStat}px">${stat.baseStat}</span>
              </div>
            </div>
          `).join('')}
        </div>
      </div>
    `;
    } catch (e) {
        error.textContent = '포켓몬 정보를 불러오는 데 실패했습니다.';
    }
}
