/**
 * PetGuardian - Console Administrativo Executivo
 * Autenticação JWT RSA-2048, Métricas Consolidadas, Gestão CRUD Completa & NoSQL MongoDB Atlas
 */

(() => {
  'use strict';

  // =========================================================================
  // 1. ESTADO GLOBAL DA APLICAÇÃO
  // =========================================================================
  const state = {
    apiUrl: localStorage.getItem('pg_admin_api_url') || 'https://app-petguardian-01bd2f0c.azurewebsites.net',
    token: localStorage.getItem('pg_admin_token') || '',
    user: JSON.parse(localStorage.getItem('pg_admin_user') || 'null'),

    // Dados em Memória
    usuarios: [],
    pets: [],
    tarefas: [],
    trilhas: [],
    modulos: [],
    aulas: [],
    conteudosMongo: [],
    actuatorHealth: null,
    refreshIntervalId: null,

    // Filtros e Pesquisa
    userSearch: '',
    userRoleFilter: 'ALL',
    petSearch: '',
    taskSearch: '',
    taskStatusFilter: 'ALL',
    trilhaSearch: '',
    moduloSearch: '',
    aulaSearch: '',
    conteudoSearch: '',

    // Navegação Ativa
    activeTab: 'tab-dashboard',
    activeSubTab: 'view-trilhas'
  };

  // =========================================================================
  // 2. REFERÊNCIAS DO DOM (CACHE)
  // =========================================================================
  const dom = {
    // Auth & Modais Gerais
    loginModal: document.getElementById('login-modal'),
    loginForm: document.getElementById('login-form'),
    loginEmail: document.getElementById('login-email'),
    loginPassword: document.getElementById('login-password'),
    apiUrlInput: document.getElementById('api-url-input'),
    btnPingApi: document.getElementById('btn-ping-api'),
    apiStatusHint: document.getElementById('api-status-hint'),
    btnSubmitLogin: document.getElementById('btn-submit-login'),
    btnFillAdmin: document.getElementById('btn-fill-admin'),
    btnEnvAzure: document.getElementById('btn-env-azure'),
    btnEnvLocalhost: document.getElementById('btn-env-localhost'),
    selectEnvTopbar: document.getElementById('select-env-topbar'),

    // Modal de Confirmação Executiva
    confirmModal: document.getElementById('confirm-modal'),
    confirmModalTitle: document.getElementById('confirm-modal-title'),
    confirmModalMessage: document.getElementById('confirm-modal-message'),
    confirmActionText: document.getElementById('confirm-action-text'),
    btnConfirmAction: document.getElementById('btn-confirm-action'),
    btnConfirmCancel: document.getElementById('btn-confirm-cancel'),
    btnCloseConfirm: document.getElementById('btn-close-confirm'),

    // Modais CRUD
    careCircleModal: document.getElementById('care-circle-modal'),
    careCircleContent: document.getElementById('care-circle-content'),
    careCircleTitle: document.getElementById('care-circle-title'),
    careCircleSubtitle: document.getElementById('care-circle-subtitle'),
    btnCloseCareCircle: document.getElementById('btn-close-care-circle'),

    prontuarioModal: document.getElementById('prontuario-modal'),
    prontuarioContent: document.getElementById('prontuario-content'),
    prontuarioTitle: document.getElementById('prontuario-title'),
    prontuarioSubtitle: document.getElementById('prontuario-subtitle'),

    userModal: document.getElementById('user-modal'),
    formUser: document.getElementById('form-user'),
    userModalTitle: document.getElementById('user-modal-title'),
    userId: document.getElementById('user-id'),
    userNome: document.getElementById('user-nome'),
    userEmail: document.getElementById('user-email'),
    userSenha: document.getElementById('user-senha'),
    btnToggleUserSenha: document.getElementById('btn-toggle-user-senha'),
    userSenhaRequired: document.getElementById('user-senha-required'),
    userSenhaHint: document.getElementById('user-senha-hint'),
    userDdd: document.getElementById('user-ddd'),
    userTelefone: document.getElementById('user-telefone'),
    userCep: document.getElementById('user-cep'),
    cepLoader: document.getElementById('cep-loader'),
    userNumero: document.getElementById('user-numero'),
    userRole: document.getElementById('user-role'),
    userCidade: document.getElementById('user-cidade'),
    userBairro: document.getElementById('user-bairro'),
    userUf: document.getElementById('user-uf'),

    petModal: document.getElementById('pet-modal'),
    formPet: document.getElementById('form-pet'),
    petModalTitle: document.getElementById('pet-modal-title'),
    petId: document.getElementById('pet-id'),
    petNome: document.getElementById('pet-nome'),
    petDataNasc: document.getElementById('pet-data-nasc'),
    petRaca: document.getElementById('pet-raca'),
    petPorte: document.getElementById('pet-porte'),
    petSexo: document.getElementById('pet-sexo'),
    petCastrado: document.getElementById('pet-castrado'),

    taskModal: document.getElementById('task-modal'),
    formTask: document.getElementById('form-task'),
    taskModalTitle: document.getElementById('task-modal-title'),
    taskId: document.getElementById('task-id'),
    taskTitulo: document.getElementById('task-titulo'),
    taskDescricao: document.getElementById('task-descricao'),
    taskPetId: document.getElementById('task-pet-id'),
    taskPrazo: document.getElementById('task-prazo'),
    taskPontos: document.getElementById('task-pontos'),
    taskStatus: document.getElementById('task-status'),

    trilhaModal: document.getElementById('trilha-modal'),
    formTrilha: document.getElementById('form-trilha'),
    trilhaModalTitle: document.getElementById('trilha-modal-title'),
    trilhaId: document.getElementById('trilha-id'),
    trilhaNome: document.getElementById('trilha-nome'),
    trilhaDescricao: document.getElementById('trilha-descricao'),
    trilhaPetId: document.getElementById('trilha-pet-id'),

    moduloModal: document.getElementById('modulo-modal'),
    formModulo: document.getElementById('form-modulo'),
    moduloModalTitle: document.getElementById('modulo-modal-title'),
    moduloId: document.getElementById('modulo-id'),
    moduloTrilhaId: document.getElementById('modulo-trilha-id'),
    moduloNome: document.getElementById('modulo-nome'),
    moduloTempo: document.getElementById('modulo-tempo'),
    moduloDescricao: document.getElementById('modulo-descricao'),

    aulaModal: document.getElementById('aula-modal'),
    formAula: document.getElementById('form-aula'),
    aulaModalTitle: document.getElementById('aula-modal-title'),
    aulaId: document.getElementById('aula-id'),
    aulaModuloId: document.getElementById('aula-modulo-id'),
    aulaNome: document.getElementById('aula-nome'),
    aulaDescricao: document.getElementById('aula-descricao'),
    aulaPontos: document.getElementById('aula-pontos'),
    aulaDificuldade: document.getElementById('aula-dificuldade'),
    aulaConcluida: document.getElementById('aula-concluida'),

    conteudoModal: document.getElementById('conteudo-modal'),
    formConteudo: document.getElementById('form-conteudo'),
    conteudoAulaId: document.getElementById('conteudo-aula-id'),
    conteudoModalSubtitle: document.getElementById('conteudo-modal-subtitle'),
    conteudoTipo: document.getElementById('conteudo-tipo'),
    conteudoLinks: document.getElementById('conteudo-links'),
    conteudoMarkdown: document.getElementById('conteudo-markdown'),
    conteudoPreview: document.getElementById('conteudo-preview'),
    btnDeleteConteudo: document.getElementById('btn-delete-conteudo'),

    // Shell & Topbar
    appContainer: document.getElementById('app-container'),
    toastContainer: document.getElementById('toast-container'),
    adminAvatar: document.getElementById('admin-avatar'),
    adminUserName: document.getElementById('admin-user-name'),
    btnLogout: document.getElementById('btn-logout'),
    pageTitle: document.getElementById('page-title'),
    pageSubtitle: document.getElementById('page-subtitle'),
    systemHealthPill: document.getElementById('system-health-pill'),
    systemHealthText: document.getElementById('system-health-text'),
    selectRefresh: document.getElementById('select-refresh'),
    btnRefreshAll: document.getElementById('btn-refresh-all'),

    // Badges Sidebar
    badgeUsersCount: document.getElementById('badge-users-count'),
    badgePetsCount: document.getElementById('badge-pets-count'),
    badgeTasksCount: document.getElementById('badge-tasks-count'),
    badgeTrilhasCount: document.getElementById('badge-trilhas-count'),

    // KPIs Dashboard
    kpiTotalUsuarios: document.getElementById('kpi-total-usuarios'),
    kpiUsuariosBreakdown: document.getElementById('kpi-usuarios-breakdown'),
    barConversionPremium: document.getElementById('bar-conversion-premium'),
    kpiConversionText: document.getElementById('kpi-conversion-text'),
    kpiTotalPets: document.getElementById('kpi-total-pets'),
    kpiPetsBreakdown: document.getElementById('kpi-pets-breakdown'),
    barCastrados: document.getElementById('bar-castrados'),
    kpiCastradosText: document.getElementById('kpi-castrados-text'),
    kpiTotalTarefas: document.getElementById('kpi-total-tarefas'),
    kpiTarefasBreakdown: document.getElementById('kpi-tarefas-breakdown'),
    barTaxaConclusao: document.getElementById('bar-taxa-conclusao'),
    kpiTarefasText: document.getElementById('kpi-tarefas-text'),
    kpiTotalPontos: document.getElementById('kpi-total-pontos'),

    // Gráficos Donut
    donutSegmentAdmin: document.getElementById('donut-segment-admin'),
    donutSegmentPremium: document.getElementById('donut-segment-premium'),
    donutSegmentComum: document.getElementById('donut-segment-comum'),
    donutRolesTotal: document.getElementById('donut-roles-total'),
    legendAdminCount: document.getElementById('legend-admin-count'),
    legendPremiumCount: document.getElementById('legend-premium-count'),
    legendComumCount: document.getElementById('legend-comum-count'),

    donutSegmentConcluido: document.getElementById('donut-segment-concluido'),
    donutSegmentPendente: document.getElementById('donut-segment-pendente'),
    donutSegmentExpirado: document.getElementById('donut-segment-expirado'),
    donutTasksTotal: document.getElementById('donut-tasks-total'),
    legendConcluidoCount: document.getElementById('legend-concluido-count'),
    legendPendenteCount: document.getElementById('legend-pendente-count'),
    legendExpiradoCount: document.getElementById('legend-expirado-count'),

    // Resumo Executivo
    metricAvgPets: document.getElementById('metric-avg-pets'),
    metricTaskEngagement: document.getElementById('metric-task-engagement'),
    miniBarPorte: document.getElementById('mini-bar-porte'),
    metricMongoConteudos: document.getElementById('metric-mongo-conteudos'),

    // Tabelas
    inputSearchUser: document.getElementById('input-search-user'),
    tbodyUsuarios: document.getElementById('tbody-usuarios'),
    btnOpenNewUser: document.getElementById('btn-open-new-user'),

    inputSearchPet: document.getElementById('input-search-pet'),
    tbodyPets: document.getElementById('tbody-pets'),
    btnOpenNewPet: document.getElementById('btn-open-new-pet'),

    inputSearchTask: document.getElementById('input-search-task'),
    tbodyTarefas: document.getElementById('tbody-tarefas'),
    btnOpenNewTask: document.getElementById('btn-open-new-task'),

    // Sub-abas de Trilhas & NoSQL
    tbodyTrilhas: document.getElementById('tbody-trilhas'),
    tbodyModulos: document.getElementById('tbody-modulos'),
    tbodyAulas: document.getElementById('tbody-aulas'),
    tbodyConteudos: document.getElementById('tbody-conteudos'),

    inputSearchTrilha: document.getElementById('input-search-trilha'),
    inputSearchModulo: document.getElementById('input-search-modulo'),
    inputSearchAula: document.getElementById('input-search-aula'),
    inputSearchConteudo: document.getElementById('input-search-conteudo'),

    btnOpenNewTrilha: document.getElementById('btn-open-new-trilha'),
    btnOpenNewModulo: document.getElementById('btn-open-new-modulo'),
    btnOpenNewAula: document.getElementById('btn-open-new-aula'),
    btnOpenNewConteudo: document.getElementById('btn-open-new-conteudo')
  };

  // =========================================================================
  // 3. UTILITÁRIOS, SEGURANÇA E PARSER MARKDOWN
  // =========================================================================

  function escapeHtml(str) {
    if (str === null || str === undefined) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  function formatPhone(ddd, numero) {
    if (!ddd || !numero) return 'Não informado';
    const cleanNum = String(numero).replace(/\D/g, '');
    if (cleanNum.length === 9) {
      return `(${ddd}) ${cleanNum.slice(0, 5)}-${cleanNum.slice(5)}`;
    }
    return `(${ddd}) ${cleanNum}`;
  }

  function formatDate(isoString) {
    if (!isoString) return '-';
    try {
      const date = new Date(isoString);
      if (isNaN(date.getTime())) return isoString;
      return new Intl.DateTimeFormat('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      }).format(date);
    } catch {
      return isoString;
    }
  }

  function renderSimpleMarkdown(md) {
    if (!md) return '<p class="text-tertiary">Nenhum conteúdo em Markdown digitado.</p>';
    let html = escapeHtml(md);

    // Blocos de código ```code```
    html = html.replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>');
    // Código inline `code`
    html = html.replace(/`([^`]+)`/g, '<code>$1</code>');
    // Títulos
    html = html.replace(/^### (.*$)/gim, '<h3>$1</h3>');
    html = html.replace(/^## (.*$)/gim, '<h2>$1</h2>');
    html = html.replace(/^# (.*$)/gim, '<h1>$1</h1>');
    // Blockquote
    html = html.replace(/^\> (.*$)/gim, '<blockquote>$1</blockquote>');
    // Negrito e Itálico
    html = html.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');
    html = html.replace(/\*([^*]+)\*/g, '<em>$1</em>');
    // Links [texto](url)
    html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank" rel="noopener noreferrer">$1</a>');
    // Itens de Lista -
    html = html.replace(/^\- (.*$)/gim, '<li>$1</li>');
    // Quebras de linha
    html = html.replace(/\n\n/g, '<br><br>');

    return html;
  }

  function showToast(message, type = 'info', duration = 3800) {
    if (!dom.toastContainer) return;
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;

    const svgIcons = {
      success: '<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>',
      error: '<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>',
      warning: '<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>',
      info: '<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>'
    };

    toast.innerHTML = `
      <span class="toast-icon">${svgIcons[type] || svgIcons.info}</span>
      <div class="toast-message">${escapeHtml(message)}</div>
      <button type="button" class="toast-close" aria-label="Fechar">&times;</button>
    `;

    toast.querySelector('.toast-close').addEventListener('click', () => {
      toast.classList.add('toast-fade-out');
      setTimeout(() => toast.remove(), 200);
    });

    dom.toastContainer.appendChild(toast);

    setTimeout(() => {
      if (toast.parentElement) {
        toast.classList.add('toast-fade-out');
        setTimeout(() => toast.remove(), 200);
      }
    }, duration);
  }

  // =========================================================================
  // 3.1. DIÁLOGO EXECUTIVO DE CONFIRMAÇÃO (SEM ALERT/CONFIRM NATIVO)
  // =========================================================================

  let confirmDialogResolver = null;

  function showConfirmDialog({ title = 'Confirmação', message, confirmText = 'Sim, Confirmar', isDanger = true } = {}) {
    if (!dom.confirmModal) return Promise.resolve(false);

    dom.confirmModalTitle.textContent = title;
    dom.confirmModalMessage.textContent = message || 'Tem certeza que deseja prosseguir com esta ação?';
    dom.confirmActionText.textContent = confirmText;

    if (isDanger) {
      dom.btnConfirmAction.className = 'btn btn-danger';
    } else {
      dom.btnConfirmAction.className = 'btn btn-primary';
    }

    dom.confirmModal.classList.add('active');

    return new Promise((resolve) => {
      confirmDialogResolver = resolve;
    });
  }

  function closeConfirmDialog(result = false) {
    if (dom.confirmModal) {
      dom.confirmModal.classList.remove('active');
    }
    if (confirmDialogResolver) {
      const resolver = confirmDialogResolver;
      confirmDialogResolver = null;
      resolver(result);
    }
  }

  // =========================================================================
  // 4. CLIENTE HTTP REST COM INJEÇÃO JWT
  // =========================================================================

  async function apiFetch(endpoint, options = {}) {
    const url = `${state.apiUrl.replace(/\/$/, '')}${endpoint}`;
    const headers = {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      ...(options.headers || {})
    };

    if (state.token) {
      headers['Authorization'] = `Bearer ${state.token}`;
    }

    const timeoutMs = options.timeout ?? 8000;
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), timeoutMs);

    try {
      const response = await fetch(url, {
        ...options,
        signal: options.signal || controller.signal,
        headers
      });
      clearTimeout(timeoutId);

      if (response.status === 401 || (response.status === 403 && endpoint !== '/actuator/health')) {
        if (state.token) {
          showToast('Sessão expirada. Faça login novamente.', 'warning');
          handleLogout();
        }
        throw new Error('Não autorizado (401/403)');
      }

      if (response.status === 204) {
        return null;
      }

      const contentType = response.headers.get('content-type') || '';
      let data = null;
      if (contentType.includes('application/json')) {
        data = await response.json();
      } else {
        data = await response.text();
      }

      if (!response.ok) {
        let errorMsg = `Erro HTTP ${response.status}`;
        if (data && typeof data === 'object') {
          errorMsg = data.message || data.error || (data.errors ? JSON.stringify(data.errors) : errorMsg);
        } else if (typeof data === 'string' && data.trim()) {
          errorMsg = data;
        }
        throw new Error(errorMsg);
      }

      return data;
    } catch (err) {
      clearTimeout(timeoutId);
      if (err.name === 'AbortError') {
        throw new Error(`Tempo limite excedido (${timeoutMs / 1000}s).`);
      }
      console.warn(`[API] Falha em ${endpoint}:`, err.message);
      throw err;
    }
  }

  // =========================================================================
  // 5. AUTENTICAÇÃO
  // =========================================================================

  async function handleLogin() {
    const email = dom.loginEmail.value.trim();
    const password = dom.loginPassword.value;
    const customApiUrl = dom.apiUrlInput.value.trim();

    if (!email || !password) {
      showToast('Informe o e-mail e a senha do administrador.', 'warning');
      return;
    }

    if (customApiUrl) {
      state.apiUrl = customApiUrl;
      localStorage.setItem('pg_admin_api_url', customApiUrl);
    }

    const btnText = dom.btnSubmitLogin.querySelector('.btn-text');
    const spinner = dom.btnSubmitLogin.querySelector('.btn-spinner');
    if (btnText) btnText.textContent = 'Verificando...';
    if (spinner) spinner.classList.remove('hidden');
    dom.btnSubmitLogin.disabled = true;

    try {
      const res = await apiFetch('/login', {
        method: 'POST',
        body: JSON.stringify({ email, senha: password })
      });

      if (!res.token || !res.user) {
        throw new Error('Resposta de autenticação inválida.');
      }

      const userRole = (res.user.role || '').toUpperCase();
      if (userRole !== 'ADMIN') {
        throw new Error(`Acesso negado: Perfil '${userRole}' não possui privilégios de administrador.`);
      }

      state.token = res.token;
      state.user = res.user;

      localStorage.setItem('pg_admin_token', res.token);
      localStorage.setItem('pg_admin_user', JSON.stringify(res.user));

      showToast(`Bem-vindo, ${res.user.nome || 'Administrador'}.`, 'success');

      dom.loginModal.classList.remove('active');
      dom.appContainer.classList.remove('hidden');

      renderAdminProfile();
      await loadAllData();
      setupAutoRefresh();
    } catch (err) {
      showToast(err.message || 'Falha na autenticação. Verifique suas credenciais.', 'error');
    } finally {
      if (btnText) btnText.textContent = 'Entrar no Console';
      if (spinner) spinner.classList.add('hidden');
      dom.btnSubmitLogin.disabled = false;
    }
  }

  function handleLogout() {
    state.token = '';
    state.user = null;
    state.usuarios = [];
    state.pets = [];
    state.tarefas = [];
    state.trilhas = [];
    state.modulos = [];
    state.aulas = [];
    state.conteudosMongo = [];

    localStorage.removeItem('pg_admin_token');
    localStorage.removeItem('pg_admin_user');

    if (state.refreshIntervalId) {
      clearInterval(state.refreshIntervalId);
      state.refreshIntervalId = null;
    }

    dom.appContainer.classList.add('hidden');
    dom.loginModal.classList.add('active');
    showToast('Sessão encerrada com sucesso.', 'info');
  }

  async function pingApi() {
    const url = dom.apiUrlInput.value.trim() || 'http://localhost:8080';
    dom.apiStatusHint.textContent = 'Verificando...';
    dom.apiStatusHint.className = 'status-indicator-tag';

    try {
      const start = performance.now();
      const res = await fetch(`${url.replace(/\/$/, '')}/actuator/health`, { method: 'GET' });
      const elapsed = Math.round(performance.now() - start);

      if (res.ok) {
        dom.apiStatusHint.textContent = `Online (${elapsed}ms)`;
        dom.apiStatusHint.className = 'status-indicator-tag is-online';
        showToast(`API conectada (${elapsed}ms)`, 'success');
      } else {
        dom.apiStatusHint.textContent = `HTTP ${res.status}`;
        dom.apiStatusHint.className = 'status-indicator-tag is-offline';
      }
    } catch {
      dom.apiStatusHint.textContent = 'Indisponível';
      dom.apiStatusHint.className = 'status-indicator-tag is-offline';
      showToast('Servidor inacessível.', 'error');
    }
  }

  function renderAdminProfile() {
    if (!state.user) return;
    const name = state.user.nome || 'Administrador';
    dom.adminUserName.textContent = name;
    const initials = name.split(' ').map(n => n[0]).slice(0, 2).join('').toUpperCase();
    dom.adminAvatar.textContent = initials || 'AD';
  }

  // =========================================================================
  // 6. MOTOR DE CARGA E CONSOLIDAÇÃO MULTI-SERVIÇOS
  // =========================================================================

  async function loadAllData() {
    dom.btnRefreshAll.classList.add('btn-spinning');
    updateHealthStatus('loading', 'Sincronizando...');

    try {
      const [usersRes, petsRes, tasksRes, trilhasRes, modulosRes, aulasRes, mongoRes, healthRes] = await Promise.allSettled([
        apiFetch('/usuarios?page=0&size=200'),
        apiFetch('/pets?page=0&size=200'),
        apiFetch('/tarefas?page=0&size=200'),
        apiFetch('/trilhas?page=0&size=100'),
        apiFetch('/modulos?page=0&size=100'),
        apiFetch('/aulas?page=0&size=100'),
        apiFetch('/aulas/conteudos?page=0&size=100'),
        apiFetch('/actuator/health')
      ]);

      if (usersRes.status === 'fulfilled' && usersRes.value) {
        state.usuarios = Array.isArray(usersRes.value) ? usersRes.value : (usersRes.value.content || []);
      }
      if (petsRes.status === 'fulfilled' && petsRes.value) {
        state.pets = Array.isArray(petsRes.value) ? petsRes.value : (petsRes.value.content || []);
      }
      if (tasksRes.status === 'fulfilled' && tasksRes.value) {
        state.tarefas = Array.isArray(tasksRes.value) ? tasksRes.value : (tasksRes.value.content || []);
      }
      if (trilhasRes.status === 'fulfilled' && trilhasRes.value) {
        state.trilhas = Array.isArray(trilhasRes.value) ? trilhasRes.value : (trilhasRes.value.content || []);
      }
      if (modulosRes.status === 'fulfilled' && modulosRes.value) {
        state.modulos = Array.isArray(modulosRes.value) ? modulosRes.value : (modulosRes.value.content || []);
      }
      if (aulasRes.status === 'fulfilled' && aulasRes.value) {
        state.aulas = Array.isArray(aulasRes.value) ? aulasRes.value : (aulasRes.value.content || []);
      }
      if (mongoRes.status === 'fulfilled' && mongoRes.value) {
        state.conteudosMongo = Array.isArray(mongoRes.value) ? mongoRes.value : (mongoRes.value.content || []);
      }

      if (healthRes.status === 'fulfilled' && healthRes.value) {
        state.actuatorHealth = healthRes.value;
        updateHealthStatus('up', 'Operacional');
      } else {
        updateHealthStatus('down', 'Atenção aos Serviços');
      }

      // Atualizar contadores
      dom.badgeUsersCount.textContent = state.usuarios.length;
      dom.badgePetsCount.textContent = state.pets.length;
      dom.badgeTasksCount.textContent = state.tarefas.length;
      dom.badgeTrilhasCount.textContent = state.trilhas.length;

      // Executar Cálculos e Renderizações
      computeAndRenderMetrics();
      renderUsersTable();
      renderPetsTable();
      renderTasksTable();
      renderTrilhasTables();

      // Atualizar dropdowns nos formulários
      populateSelectOptions();

    } catch (err) {
      console.error('Falha de sincronização:', err);
      showToast('Falha parcial na sincronização de dados.', 'warning');
    } finally {
      dom.btnRefreshAll.classList.remove('btn-spinning');
    }
  }

  function updateHealthStatus(status, text) {
    dom.systemHealthPill.className = `live-status-pill pulse-${status}`;
    dom.systemHealthText.textContent = text;
  }

  function populateSelectOptions() {
    // 1. Dropdown de Pets nas Tarefas e Trilhas
    const petOptions = state.pets.map(p => `<option value="${p.id}">#${p.id} - ${escapeHtml(p.nome)} (${escapeHtml(p.raca || 'SRD')})</option>`).join('');
    if (dom.taskPetId) dom.taskPetId.innerHTML = '<option value="">Selecione um pet...</option>' + petOptions;
    if (dom.trilhaPetId) dom.trilhaPetId.innerHTML = '<option value="">Selecione um pet...</option>' + petOptions;

    // 2. Dropdown de Trilhas nos Módulos
    const trilhaOptions = state.trilhas.map(t => `<option value="${t.id}">#${t.id} - ${escapeHtml(t.nome)}</option>`).join('');
    if (dom.moduloTrilhaId) dom.moduloTrilhaId.innerHTML = '<option value="">Selecione uma trilha...</option>' + trilhaOptions;

    // 3. Dropdown de Módulos nas Aulas
    const moduloOptions = state.modulos.map(m => `<option value="${m.id}">#${m.id} - ${escapeHtml(m.nome)}</option>`).join('');
    if (dom.aulaModuloId) dom.aulaModuloId.innerHTML = '<option value="">Selecione um módulo...</option>' + moduloOptions;
  }

  // =========================================================================
  // 7. CÁLCULO E RENDERIZAÇÃO DE INDICADORES (BI)
  // =========================================================================

  function computeAndRenderMetrics() {
    const totalUsers = state.usuarios.length;
    const totalPets = state.pets.length;
    const totalTarefas = state.tarefas.length;
    const totalConteudos = state.conteudosMongo.length;

    let countAdmin = 0;
    let countPremium = 0;
    let countComum = 0;

    state.usuarios.forEach(u => {
      const r = (u.role || '').toUpperCase();
      if (r === 'ADMIN') countAdmin++;
      else if (r === 'PREMIUM') countPremium++;
      else countComum++;
    });

    const taxaPremium = totalUsers > 0 ? ((countPremium / totalUsers) * 100).toFixed(1) : 0;

    dom.kpiTotalUsuarios.textContent = totalUsers;
    dom.kpiUsuariosBreakdown.textContent = `${countComum} Comum • ${countPremium} Premium • ${countAdmin} Admin`;
    dom.barConversionPremium.style.width = `${Math.min(taxaPremium, 100)}%`;
    dom.kpiConversionText.textContent = `Conversão Premium: ${taxaPremium}% (${countPremium}/${totalUsers})`;

    renderDonutChartRoles(totalUsers, countAdmin, countPremium, countComum);

    const castrados = state.pets.filter(p => p.castrado).length;
    const taxaCastracao = totalPets > 0 ? ((castrados / totalPets) * 100).toFixed(1) : 0;
    const avgPetsPerUser = totalUsers > 0 ? (totalPets / totalUsers).toFixed(2) : '0.00';

    dom.kpiTotalPets.textContent = totalPets;
    dom.kpiPetsBreakdown.textContent = `Média: ${avgPetsPerUser} pets por tutor`;
    dom.barCastrados.style.width = `${Math.min(taxaCastracao, 100)}%`;
    dom.kpiCastradosText.textContent = `Castrados: ${taxaCastracao}% (${castrados}/${totalPets})`;

    let tarefasConcluidas = 0;
    let tarefasPendentes = 0;
    let tarefasExpiradas = 0;
    let pontosAcumuladosTarefas = 0;

    state.tarefas.forEach(t => {
      const st = (t.status || '').toUpperCase();
      if (st === 'CONCLUIDO') {
        tarefasConcluidas++;
        pontosAcumuladosTarefas += (Number(t.pontosTarefa) || 0);
      } else if (st === 'EXPIRADO') {
        tarefasExpiradas++;
      } else {
        tarefasPendentes++;
      }
    });

    const taxaConclusao = totalTarefas > 0 ? ((tarefasConcluidas / totalTarefas) * 100).toFixed(1) : 0;

    dom.kpiTotalTarefas.textContent = totalTarefas;
    dom.kpiTarefasBreakdown.textContent = `${tarefasConcluidas} concluídas • ${tarefasPendentes} pendentes`;
    dom.barTaxaConclusao.style.width = `${Math.min(taxaConclusao, 100)}%`;
    dom.kpiTarefasText.textContent = `Taxa de Conclusão: ${taxaConclusao}%`;

    renderDonutChartTasks(totalTarefas, tarefasConcluidas, tarefasPendentes, tarefasExpiradas);

    dom.kpiTotalPontos.textContent = `${pontosAcumuladosTarefas.toLocaleString('pt-BR')} pts`;

    dom.metricAvgPets.textContent = avgPetsPerUser;
    dom.metricTaskEngagement.textContent = `${taxaConclusao}%`;
    dom.metricMongoConteudos.textContent = totalConteudos;

    renderPorteDistribution(totalPets);
  }

  function renderDonutChartRoles(total, admin, premium, comum) {
    dom.donutRolesTotal.textContent = total;
    dom.legendAdminCount.textContent = admin;
    dom.legendPremiumCount.textContent = premium;
    dom.legendComumCount.textContent = comum;

    if (total === 0) {
      dom.donutSegmentAdmin.setAttribute('stroke-dasharray', '0 100');
      dom.donutSegmentPremium.setAttribute('stroke-dasharray', '0 100');
      dom.donutSegmentComum.setAttribute('stroke-dasharray', '0 100');
      return;
    }

    const pAdmin = (admin / total) * 100;
    const pPremium = (premium / total) * 100;
    const pComum = (comum / total) * 100;

    dom.donutSegmentAdmin.setAttribute('stroke-dasharray', `${pAdmin.toFixed(2)} ${Math.max(0, 100 - pAdmin).toFixed(2)}`);
    dom.donutSegmentAdmin.setAttribute('stroke-dashoffset', '0');

    dom.donutSegmentPremium.setAttribute('stroke-dasharray', `${pPremium.toFixed(2)} ${Math.max(0, 100 - pPremium).toFixed(2)}`);
    dom.donutSegmentPremium.setAttribute('stroke-dashoffset', `-${pAdmin.toFixed(2)}`);

    dom.donutSegmentComum.setAttribute('stroke-dasharray', `${pComum.toFixed(2)} ${Math.max(0, 100 - pComum).toFixed(2)}`);
    dom.donutSegmentComum.setAttribute('stroke-dashoffset', `-${(pAdmin + pPremium).toFixed(2)}`);
  }

  function renderDonutChartTasks(total, concluidas, pendentes, expiradas) {
    dom.donutTasksTotal.textContent = total;
    dom.legendConcluidoCount.textContent = concluidas;
    dom.legendPendenteCount.textContent = pendentes;
    dom.legendExpiradoCount.textContent = expiradas;

    if (total === 0) {
      dom.donutSegmentConcluido.setAttribute('stroke-dasharray', '0 100');
      dom.donutSegmentPendente.setAttribute('stroke-dasharray', '0 100');
      dom.donutSegmentExpirado.setAttribute('stroke-dasharray', '0 100');
      return;
    }

    const pConcluido = (concluidas / total) * 100;
    const pPendente = (pendentes / total) * 100;
    const pExpirado = (expiradas / total) * 100;

    dom.donutSegmentConcluido.setAttribute('stroke-dasharray', `${pConcluido.toFixed(2)} ${Math.max(0, 100 - pConcluido).toFixed(2)}`);
    dom.donutSegmentConcluido.setAttribute('stroke-dashoffset', '0');

    dom.donutSegmentPendente.setAttribute('stroke-dasharray', `${pPendente.toFixed(2)} ${Math.max(0, 100 - pPendente).toFixed(2)}`);
    dom.donutSegmentPendente.setAttribute('stroke-dashoffset', `-${pConcluido.toFixed(2)}`);

    dom.donutSegmentExpirado.setAttribute('stroke-dasharray', `${pExpirado.toFixed(2)} ${Math.max(0, 100 - pExpirado).toFixed(2)}`);
    dom.donutSegmentExpirado.setAttribute('stroke-dashoffset', `-${(pConcluido + pPendente).toFixed(2)}`);
  }

  function renderPorteDistribution(totalPets) {
    if (!dom.miniBarPorte) return;
    const porteCount = {
      PEQUENO: 0,
      MEDIO: 0,
      GRANDE: 0,
      GIGANTE: 0
    };

    state.pets.forEach(p => {
      const porte = (p.porte || '').toUpperCase();
      if (porteCount.hasOwnProperty(porte)) {
        porteCount[porte]++;
      } else {
        porteCount.MEDIO++;
      }
    });

    const labels = {
      PEQUENO: 'Pequeno',
      MEDIO: 'Médio',
      GRANDE: 'Grande',
      GIGANTE: 'Gigante'
    };

    dom.miniBarPorte.innerHTML = Object.entries(porteCount).map(([porte, count]) => {
      const pct = totalPets > 0 ? Math.round((count / totalPets) * 100) : 0;
      return `
        <div class="mini-bar-item">
          <div class="mini-bar-header">
            <span>${labels[porte] || porte}</span>
            <span><strong>${count}</strong> (${pct}%)</span>
          </div>
          <div class="mini-bar-track">
            <div class="mini-bar-progress" style="width: ${pct}%"></div>
          </div>
        </div>
      `;
    }).join('');
  }

  // =========================================================================
  // 8. CRUD USUÁRIOS & VIACEP
  // =========================================================================

  function renderUsersTable() {
    if (!dom.tbodyUsuarios) return;

    let filtered = state.usuarios.filter(u => {
      const matchesRole = state.userRoleFilter === 'ALL' || (u.role || '').toUpperCase() === state.userRoleFilter;
      const q = state.userSearch.toLowerCase();
      return matchesRole && (!q ||
        (u.nome || '').toLowerCase().includes(q) ||
        (u.email || '').toLowerCase().includes(q) ||
        (u.endereco && u.endereco.cidade && u.endereco.cidade.toLowerCase().includes(q)));
    });

    if (filtered.length === 0) {
      dom.tbodyUsuarios.innerHTML = `<tr><td colspan="6" class="table-cell-empty">Nenhum usuário corresponde aos critérios.</td></tr>`;
      return;
    }

    dom.tbodyUsuarios.innerHTML = filtered.map(u => {
      const initials = (u.nome || 'U').split(' ').map(n => n[0]).slice(0, 2).join('').toUpperCase();
      const role = (u.role || 'COMUM').toUpperCase();
      const roleBadgeClass = role === 'ADMIN' ? 'role-admin' : (role === 'PREMIUM' ? 'role-premium' : 'role-comum');

      let localidade = '<span class="text-tertiary">Não informada</span>';
      if (u.endereco) {
        localidade = `
          <div class="address-cell-wrap">
            <strong>${escapeHtml(u.endereco.cidade || '')}/${escapeHtml(u.endereco.uf || '')}</strong>
            <small>${escapeHtml(u.endereco.bairro || '')} • CEP ${escapeHtml(u.endereco.cep || '')}</small>
          </div>
        `;
      }

      return `
        <tr data-user-id="${u.id}">
          <td class="font-mono">#${u.id}</td>
          <td>
            <div class="user-profile-cell">
              <div class="user-avatar-circle">${initials}</div>
              <div>
                <div class="user-name-title">${escapeHtml(u.nome)}</div>
                <div class="user-email-subtitle">${escapeHtml(u.email)}</div>
              </div>
            </div>
          </td>
          <td>${formatPhone(u.ddd, u.numeroTelefone)}</td>
          <td>${localidade}</td>
          <td>
            <select class="role-dropdown-select ${roleBadgeClass}" data-user-id="${u.id}">
              <option value="ADMIN" ${role === 'ADMIN' ? 'selected' : ''}>ADMIN</option>
              <option value="PREMIUM" ${role === 'PREMIUM' ? 'selected' : ''}>PREMIUM</option>
              <option value="COMUM" ${role === 'COMUM' ? 'selected' : ''}>COMUM</option>
            </select>
          </td>
          <td class="text-right">
            <div class="action-buttons-wrap">
              <button type="button" class="btn-ghost-action btn-edit-user" data-user-id="${u.id}" title="Editar Usuário">
                Editar
              </button>
              <button type="button" class="btn-ghost-action btn-care-circle" data-user-id="${u.id}" title="Ver Rede">
                Rede
              </button>
              <button type="button" class="btn-ghost-action btn-danger btn-delete-user" data-user-id="${u.id}" data-user-name="${escapeHtml(u.nome)}" title="Excluir">
                Excluir
              </button>
            </div>
          </td>
        </tr>
      `;
    }).join('');

    dom.tbodyUsuarios.querySelectorAll('.role-dropdown-select').forEach(sel => {
      sel.addEventListener('change', async (e) => {
        await updateUserRole(Number(e.target.dataset.userId), e.target.value);
      });
    });

    dom.tbodyUsuarios.querySelectorAll('.btn-edit-user').forEach(btn => {
      btn.addEventListener('click', () => openUserModal(Number(btn.dataset.userId)));
    });

    dom.tbodyUsuarios.querySelectorAll('.btn-care-circle').forEach(btn => {
      btn.addEventListener('click', () => openCareCircleModal(Number(btn.dataset.userId)));
    });

    dom.tbodyUsuarios.querySelectorAll('.btn-delete-user').forEach(btn => {
      btn.addEventListener('click', () => confirmAndDeleteUser(Number(btn.dataset.userId), btn.dataset.userName));
    });
  }

  function resetPasswordEyeIcon() {
    if (!dom.btnToggleUserSenha) return;
    const showIcon = dom.btnToggleUserSenha.querySelector('.eye-icon-show');
    const hideIcon = dom.btnToggleUserSenha.querySelector('.eye-icon-hide');
    if (showIcon) showIcon.classList.remove('hidden');
    if (hideIcon) hideIcon.classList.add('hidden');
  }

  function openUserModal(userId = null) {
    dom.formUser.reset();
    if (dom.userSenha) dom.userSenha.type = 'password';
    resetPasswordEyeIcon();

    if (userId) {
      const u = state.usuarios.find(x => x.id === userId);
      if (!u) return;
      dom.userModalTitle.textContent = `Editar Usuário #${u.id}`;
      dom.userId.value = u.id;
      dom.userNome.value = u.nome || '';
      dom.userEmail.value = u.email || '';
      dom.userSenha.value = '123mudar'; // Valor padrão para passar na validação NotBlank se não alterado
      dom.userSenhaRequired.classList.add('hidden');
      dom.userSenhaHint.textContent = 'Mantenha ou digite uma nova senha para atualizar';
      dom.userDdd.value = u.ddd || '';
      dom.userTelefone.value = u.numeroTelefone || '';
      dom.userRole.value = (u.role || 'COMUM').toUpperCase();
      if (u.endereco) {
        dom.userCep.value = u.endereco.cep || '';
        dom.userNumero.value = u.endereco.numero || '';
        dom.userCidade.value = u.endereco.cidade || '';
        dom.userBairro.value = u.endereco.bairro || '';
        dom.userUf.value = u.endereco.uf || '';
      }
    } else {
      dom.userModalTitle.textContent = 'Novo Usuário';
      dom.userId.value = '';
      dom.userSenhaRequired.classList.remove('hidden');
      dom.userSenhaHint.textContent = 'Obrigatório no cadastro';
      dom.userRole.value = 'COMUM';
    }
    dom.userModal.classList.add('active');
  }

  async function saveUserForm() {
    const id = dom.userId.value ? Number(dom.userId.value) : null;
    const nome = dom.userNome.value.trim();
    const email = dom.userEmail.value.trim();
    const senha = dom.userSenha.value;
    const ddd = dom.userDdd.value.trim();
    const numeroTelefone = dom.userTelefone.value.trim().replace(/\D/g, '');
    const cep = dom.userCep.value.trim().replace(/\D/g, '');
    const numero = dom.userNumero.value.trim();
    const role = dom.userRole.value;

    if (!nome || !email || !ddd || !numeroTelefone || !cep || !numero) {
      showToast('Preencha todos os campos obrigatórios.', 'warning');
      return;
    }

    if (!id && (!senha || senha.length < 6)) {
      showToast('A senha deve ter no mínimo 6 caracteres.', 'warning');
      return;
    }

    const payload = {
      nome,
      email,
      senha: senha || '123mudar',
      ddd,
      numeroTelefone,
      endereco: {
        cep,
        numero
      }
    };

    const btnSubmit = document.getElementById('btn-save-user');
    btnSubmit.disabled = true;

    try {
      if (id) {
        showToast(`Atualizando usuário #${id}...`, 'info');
        await apiFetch(`/usuarios/${id}`, {
          method: 'PUT',
          body: JSON.stringify(payload)
        });
        if (role) {
          await apiFetch(`/usuarios/${id}/role`, {
            method: 'PATCH',
            body: JSON.stringify({ role })
          });
        }
        showToast('Usuário atualizado com sucesso!', 'success');
      } else {
        showToast('Criando novo usuário...', 'info');
        const created = await apiFetch('/usuarios', {
          method: 'POST',
          body: JSON.stringify(payload)
        });
        if (role && role !== 'COMUM' && created && created.id) {
          await apiFetch(`/usuarios/${created.id}/role`, {
            method: 'PATCH',
            body: JSON.stringify({ role })
          });
        }
        showToast('Usuário cadastrado com sucesso!', 'success');
      }

      dom.userModal.classList.remove('active');
      await loadAllData();
    } catch (err) {
      showToast(err.message || 'Falha ao salvar usuário.', 'error');
    } finally {
      btnSubmit.disabled = false;
    }
  }

  async function updateUserRole(userId, newRole) {
    try {
      showToast(`Atualizando perfil para ${newRole}...`, 'info');
      const updated = await apiFetch(`/usuarios/${userId}/role`, {
        method: 'PATCH',
        body: JSON.stringify({ role: newRole })
      });

      const idx = state.usuarios.findIndex(u => u.id === userId);
      if (idx !== -1) {
        state.usuarios[idx] = updated;
      }

      computeAndRenderMetrics();
      renderUsersTable();
      showToast(`Perfil atualizado para ${newRole}.`, 'success');
    } catch (err) {
      showToast(err.message || 'Falha ao atualizar perfil.', 'error');
      renderUsersTable();
    }
  }

  async function confirmAndDeleteUser(userId, userName) {
    if (userId === state.user?.id) {
      showToast('Operação bloqueada: não é permitido excluir o usuário ativo.', 'warning');
      return;
    }

    const confirmed = await showConfirmDialog({
      title: 'Excluir Usuário',
      message: `Tem certeza que deseja excluir definitivamente o usuário "${userName}" (ID #${userId})? Todos os vínculos e dados associados serão removidos.`,
      confirmText: 'Sim, Excluir Usuário',
      isDanger: true
    });
    if (!confirmed) return;

    try {
      showToast(`Excluindo usuário #${userId}...`, 'info');
      await apiFetch(`/usuarios/${userId}`, { method: 'DELETE' });
      state.usuarios = state.usuarios.filter(u => u.id !== userId);
      computeAndRenderMetrics();
      renderUsersTable();
      showToast(`Usuário "${userName}" excluído.`, 'success');
    } catch (err) {
      showToast(err.message || 'Falha ao excluir usuário.', 'error');
    }
  }

  // Busca Automática ViaCEP no Formulário
  async function lookupViaCep(cepDigits) {
    const clean = cepDigits.replace(/\D/g, '');
    if (clean.length !== 8) return;

    dom.cepLoader.classList.remove('hidden');
    try {
      const res = await fetch(`https://viacep.com.br/ws/${clean}/json/`);
      if (res.ok) {
        const data = await res.json();
        if (!data.erro) {
          dom.userCidade.value = data.localidade || '';
          dom.userBairro.value = data.bairro || '';
          dom.userUf.value = data.uf || '';
          showToast(`Endereço localizado: ${data.localidade}/${data.uf}`, 'info');
        }
      }
    } catch {
      // Degradação silenciosa
    } finally {
      dom.cepLoader.classList.add('hidden');
    }
  }

  // =========================================================================
  // 9. CRUD PETS & PACIENTES
  // =========================================================================

  function renderPetsTable() {
    if (!dom.tbodyPets) return;

    let filtered = state.pets.filter(p => {
      const q = state.petSearch.toLowerCase();
      return !q ||
        (p.nome || '').toLowerCase().includes(q) ||
        (p.raca || '').toLowerCase().includes(q);
    });

    if (filtered.length === 0) {
      dom.tbodyPets.innerHTML = `<tr><td colspan="7" class="table-cell-empty">Nenhum pet encontrado.</td></tr>`;
      return;
    }

    dom.tbodyPets.innerHTML = filtered.map(p => {
      const sexoLabel = p.sexo === 'M' ? 'Macho (M)' : (p.sexo === 'F' ? 'Fêmea (F)' : '-');

      return `
        <tr>
          <td class="font-mono">#${p.id}</td>
          <td><strong>${escapeHtml(p.nome)}</strong></td>
          <td>${escapeHtml(p.raca || 'SRD')}</td>
          <td><span class="badge-porte">${escapeHtml(p.porte || 'MEDIO')}</span></td>
          <td>${sexoLabel}</td>
          <td>
            ${p.castrado
              ? '<span class="status-pill is-done"><span class="status-dot-mini"></span>Sim</span>'
              : '<span class="status-pill is-pending"><span class="status-dot-mini"></span>Não</span>'}
          </td>
          <td class="text-right">
            <div class="action-buttons-wrap">
              <button type="button" class="btn-ghost-action btn-edit-pet" data-pet-id="${p.id}">
                Editar
              </button>
              <button type="button" class="btn-ghost-action btn-view-pet" data-pet-id="${p.id}">
                Prontuário
              </button>
              <button type="button" class="btn-ghost-action btn-danger btn-delete-pet" data-pet-id="${p.id}" data-pet-name="${escapeHtml(p.nome)}">
                Excluir
              </button>
            </div>
          </td>
        </tr>
      `;
    }).join('');

    dom.tbodyPets.querySelectorAll('.btn-edit-pet').forEach(btn => {
      btn.addEventListener('click', () => openPetModal(Number(btn.dataset.petId)));
    });

    dom.tbodyPets.querySelectorAll('.btn-view-pet').forEach(btn => {
      btn.addEventListener('click', () => {
        openProntuarioModal(Number(btn.dataset.petId));
      });
    });

    dom.tbodyPets.querySelectorAll('.btn-delete-pet').forEach(btn => {
      btn.addEventListener('click', () => confirmAndDeletePet(Number(btn.dataset.petId), btn.dataset.petName));
    });
  }

  function calculateAge(birthDateStr) {
    if (!birthDateStr) return 'Idade não informada';
    try {
      const birth = new Date(birthDateStr);
      const now = new Date();
      let years = now.getFullYear() - birth.getFullYear();
      let months = now.getMonth() - birth.getMonth();
      if (months < 0) {
        years--;
        months += 12;
      }
      if (years > 0) {
        return `${years} ano${years > 1 ? 's' : ''}${months > 0 ? ` e ${months} m` : ''}`;
      }
      return `${months} mês${months !== 1 ? 'es' : ''}`;
    } catch {
      return 'Não calculada';
    }
  }

  async function openProntuarioModal(petId) {
    const pet = state.pets.find(p => p.id === Number(petId));
    if (!pet) {
      showToast('Pet não encontrado em memória.', 'warning');
      return;
    }

    dom.prontuarioTitle.textContent = `Prontuário: ${pet.nome} (#${pet.id})`;
    dom.prontuarioSubtitle.textContent = `${pet.raca || 'SRD'} • Porte ${pet.porte || 'MEDIO'} • ${pet.sexo === 'M' ? 'Macho' : 'Fêmea'}`;

    // Tarefas locais associadas a este pet
    const petTasks = state.tarefas.filter(t => t.petId === Number(petId));
    const completedTasks = petTasks.filter(t => (t.status || '').toUpperCase() === 'CONCLUIDO');
    const localEstimatedPoints = completedTasks.reduce((sum, t) => sum + (Number(t.pontosTarefa) || 0), 0);

    const sexoLabel = pet.sexo === 'M' ? 'Macho (M)' : (pet.sexo === 'F' ? 'Fêmea (F)' : 'Não informado');
    const castradoBadge = pet.castrado
      ? '<span class="status-pill is-done"><span class="status-dot-mini"></span>Sim, Castrado</span>'
      : '<span class="status-pill is-pending"><span class="status-dot-mini"></span>Não Castrado</span>';

    // RENDERIZAÇÃO INSTANTÂNEA (< 50ms) - Fim da espera de "2000 anos"
    dom.prontuarioContent.innerHTML = `
      <div class="prontuario-grid">
        <!-- Coluna 1: Dados Biométricos -->
        <div class="prontuario-card">
          <div class="prontuario-card-title">
            <span>Ficha Biométrica & Registro</span>
            <span class="badge-porte">${escapeHtml(pet.porte || 'MEDIO')}</span>
          </div>
          <div class="prontuario-bio-list">
            <div class="prontuario-bio-row">
              <span class="prontuario-bio-lbl">Nome do Paciente</span>
              <strong class="prontuario-bio-val">${escapeHtml(pet.nome)}</strong>
            </div>
            <div class="prontuario-bio-row">
              <span class="prontuario-bio-lbl">Raça</span>
              <span class="prontuario-bio-val">${escapeHtml(pet.raca || 'SRD')}</span>
            </div>
            <div class="prontuario-bio-row">
              <span class="prontuario-bio-lbl">Sexo</span>
              <span class="prontuario-bio-val">${sexoLabel}</span>
            </div>
            <div class="prontuario-bio-row">
              <span class="prontuario-bio-lbl">Nascimento / Idade</span>
              <span class="prontuario-bio-val">${formatDate(pet.dataNasc)} (${calculateAge(pet.dataNasc)})</span>
            </div>
            <div class="prontuario-bio-row">
              <span class="prontuario-bio-lbl">Castração</span>
              <span class="prontuario-bio-val">${castradoBadge}</span>
            </div>
          </div>

          <div style="margin-top: 16px;">
            <div class="prontuario-card-title">
              <span>Cuidadores & Tutores Vinculados</span>
            </div>
            <div id="prontuario-cuidadores" class="prontuario-care-chips">
              <span class="prontuario-chip font-mono">Carregando cuidadores...</span>
            </div>
          </div>
        </div>

        <!-- Coluna 2: Gamificação Oracle & Rotina -->
        <div class="prontuario-card">
          <div class="prontuario-card-title">
            <span>Pontuação Consolidada</span>
            <span id="prontuario-sync-tag" class="sync-status-badge sync-shimmer">
              <span class="status-dot-mini" style="background: currentColor;"></span>
              Consultando Oracle 19c...
            </span>
          </div>

          <div class="prontuario-score-hero">
            <div class="prontuario-score-number" id="prontuario-points-val">${localEstimatedPoints} pts</div>
            <div class="prontuario-score-caption" id="prontuario-points-lbl">Pontuação calculada via rotinas preventivas</div>
          </div>

          <div class="prontuario-card-title">
            <span>Rotina Preventiva (${petTasks.length} tarefas)</span>
            <small style="color: var(--text-tertiary);">${completedTasks.length} concluídas</small>
          </div>

          <div class="prontuario-tasks-scroll">
            ${petTasks.length === 0 ? '<p style="color: var(--text-tertiary); font-size: 12px;">Nenhuma rotina atribuída a este pet.</p>' :
              petTasks.map(t => `
                <div class="prontuario-task-item">
                  <div>
                    <strong>${escapeHtml(t.titulo)}</strong>
                    <div style="font-size: 11px; color: var(--text-tertiary);">${formatDate(t.prazo)}</div>
                  </div>
                  <div>
                    <span class="status-pill ${t.status === 'CONCLUIDO' ? 'is-done' : (t.status === 'EXPIRADO' ? 'is-expired' : 'is-pending')}">
                      ${t.status === 'CONCLUIDO' ? 'Concluída' : (t.status === 'EXPIRADO' ? 'Expirada' : 'Pendente')}
                    </span>
                  </div>
                </div>
              `).join('')
            }
          </div>
        </div>
      </div>

      <!-- Seção Inferior: Histórico Clínico Veterinário -->
      <div class="prontuario-card" style="margin-top: 16px;">
        <div class="prontuario-card-title">
          <span>Histórico Clínico & Anotações Veterinárias</span>
          <span id="prontuario-hist-count" class="badge-subtle">0 registros</span>
        </div>
        <div id="prontuario-historico-list">
          <p style="color: var(--text-tertiary); font-size: 12px;">Consultando histórico veterinário...</p>
        </div>
      </div>
    `;

    // Abre o modal instantaneamente!
    dom.prontuarioModal.classList.add('active');

    // Executa em background a busca completa com timeout de 3.5s para não travar
    try {
      const detail = await apiFetch(`/pets/${petId}/detalhe`, { timeout: 3500 });
      if (!detail) return;

      const pointsEl = document.getElementById('prontuario-points-val');
      const pointsLbl = document.getElementById('prontuario-points-lbl');
      const syncTag = document.getElementById('prontuario-sync-tag');
      const cuidadoresEl = document.getElementById('prontuario-cuidadores');
      const histEl = document.getElementById('prontuario-historico-list');
      const histCountEl = document.getElementById('prontuario-hist-count');

      if (pointsEl && detail.pontosTotais !== undefined) {
        pointsEl.textContent = `${detail.pontosTotais} pts`;
      }
      if (pointsLbl) {
        pointsLbl.textContent = `Procedimento Oracle 19c (Tarefas + Aulas Concluídas: ${detail.tarefasConcluidas || 0})`;
      }
      if (syncTag) {
        syncTag.className = 'sync-status-badge sync-success';
        syncTag.innerHTML = '✓ Sincronizado com Oracle 19c';
      }

      if (cuidadoresEl && Array.isArray(detail.cuidadores)) {
        if (detail.cuidadores.length === 0) {
          cuidadoresEl.innerHTML = '<span style="color: var(--text-tertiary); font-size: 11.5px;">Nenhum tutor registrado.</span>';
        } else {
          cuidadoresEl.innerHTML = detail.cuidadores.map(c => `
            <span class="prontuario-chip">
              <strong>${escapeHtml(c.nome || c.email)}</strong>
              ${c.responsavelPrincipal ? '<small style="color: var(--primary);"> (Principal)</small>' : ''}
            </span>
          `).join('');
        }
      }

      if (histEl && histCountEl) {
        const hist = detail.historicoClinico || [];
        histCountEl.textContent = `${hist.length} registro${hist.length !== 1 ? 's' : ''}`;
        if (hist.length === 0) {
          histEl.innerHTML = '<p style="color: var(--text-tertiary); font-size: 12px;">Nenhum registro clínico cadastrado para este pet.</p>';
        } else {
          histEl.innerHTML = hist.map(h => `
            <div style="padding: 8px 0; border-bottom: 1px solid var(--border-subtle); font-size: 12.5px;">
              <div style="display: flex; justify-content: space-between; margin-bottom: 3px;">
                <strong>${escapeHtml(h.titulo || 'Registro Clínico')}</strong>
                <small style="color: var(--text-tertiary);">${formatDate(h.dataHist)}</small>
              </div>
              <p style="color: var(--text-secondary); margin: 0;">${escapeHtml(h.descricao || '-')}</p>
            </div>
          `).join('');
        }
      }
    } catch (err) {
      console.warn(`[Prontuario] Consulta estendida ao pet #${petId} indisponível:`, err.message);
      const syncTag = document.getElementById('prontuario-sync-tag');
      const cuidadoresEl = document.getElementById('prontuario-cuidadores');
      const histEl = document.getElementById('prontuario-historico-list');

      if (syncTag) {
        syncTag.className = 'sync-status-badge sync-fallback';
        syncTag.innerHTML = 'Pontuação local estimada';
      }
      if (cuidadoresEl) {
        cuidadoresEl.innerHTML = '<span style="color: var(--text-tertiary); font-size: 11.5px;">Tutor associado via cadastro geral.</span>';
      }
      if (histEl) {
        histEl.innerHTML = '<p style="color: var(--text-tertiary); font-size: 12px;">Histórico clínico não disponível no momento.</p>';
      }
    }
  }

  function openPetModal(petId = null) {
    dom.formPet.reset();
    if (petId) {
      const p = state.pets.find(x => x.id === petId);
      if (!p) return;
      dom.petModalTitle.textContent = `Editar Pet #${p.id}`;
      dom.petId.value = p.id;
      dom.petNome.value = p.nome || '';
      dom.petDataNasc.value = p.dataNasc || '';
      dom.petRaca.value = p.raca || '';
      dom.petPorte.value = (p.porte || 'MEDIO').toUpperCase();
      dom.petSexo.value = p.sexo || 'M';
      dom.petCastrado.value = p.castrado ? 'true' : 'false';
    } else {
      dom.petModalTitle.textContent = 'Novo Pet';
      dom.petId.value = '';
      dom.petDataNasc.value = new Date().toISOString().split('T')[0];
    }
    dom.petModal.classList.add('active');
  }

  async function savePetForm() {
    const id = dom.petId.value ? Number(dom.petId.value) : null;
    const nome = dom.petNome.value.trim();
    const dataNasc = dom.petDataNasc.value;
    const raca = dom.petRaca.value.trim();
    const porte = dom.petPorte.value;
    const sexo = dom.petSexo.value;
    const castrado = dom.petCastrado.value === 'true';

    if (!nome || !dataNasc || !raca || !porte || !sexo) {
      showToast('Preencha todos os campos do pet.', 'warning');
      return;
    }

    const payload = {
      nome,
      dataNasc,
      raca,
      porte,
      sexo,
      castrado
    };

    const btnSubmit = document.getElementById('btn-save-pet');
    btnSubmit.disabled = true;

    try {
      if (id) {
        showToast(`Atualizando pet #${id}...`, 'info');
        await apiFetch(`/pets/${id}`, {
          method: 'PUT',
          body: JSON.stringify(payload)
        });
        showToast('Pet atualizado com sucesso!', 'success');
      } else {
        showToast('Cadastrando novo pet...', 'info');
        await apiFetch('/pets', {
          method: 'POST',
          body: JSON.stringify(payload)
        });
        showToast('Pet cadastrado com sucesso!', 'success');
      }

      dom.petModal.classList.remove('active');
      await loadAllData();
    } catch (err) {
      showToast(err.message || 'Falha ao salvar pet.', 'error');
    } finally {
      btnSubmit.disabled = false;
    }
  }

  async function confirmAndDeletePet(petId, petName) {
    const confirmed = await showConfirmDialog({
      title: 'Excluir Pet',
      message: `Tem certeza que deseja excluir o pet "${petName}" (ID #${petId})? Todas as rotinas e registros vinculados serão removidos.`,
      confirmText: 'Sim, Excluir Pet',
      isDanger: true
    });
    if (!confirmed) return;

    try {
      showToast(`Excluindo pet #${petId}...`, 'info');
      await apiFetch(`/pets/${petId}`, { method: 'DELETE' });
      state.pets = state.pets.filter(p => p.id !== petId);
      computeAndRenderMetrics();
      renderPetsTable();
      populateSelectOptions();
      showToast(`Pet "${petName}" excluído com sucesso.`, 'success');
    } catch (err) {
      showToast(err.message || 'Falha ao excluir pet.', 'error');
    }
  }

  // =========================================================================
  // 10. CRUD TAREFAS & ROTINA DE CUIDADOS
  // =========================================================================

  function renderTasksTable() {
    if (!dom.tbodyTarefas) return;

    let filtered = state.tarefas.filter(t => {
      const matchesStatus = state.taskStatusFilter === 'ALL' || (t.status || '').toUpperCase() === state.taskStatusFilter;
      const q = state.taskSearch.toLowerCase();
      return matchesStatus && (!q ||
        (t.titulo || '').toLowerCase().includes(q) ||
        (t.usuarioNome || '').toLowerCase().includes(q));
    });

    if (filtered.length === 0) {
      dom.tbodyTarefas.innerHTML = `<tr><td colspan="8" class="table-cell-empty">Nenhuma tarefa correspondente aos filtros.</td></tr>`;
      return;
    }

    dom.tbodyTarefas.innerHTML = filtered.map(t => {
      const status = (t.status || 'PENDENTE').toUpperCase();
      let statusHtml = '';
      if (status === 'CONCLUIDO') {
        statusHtml = '<span class="status-pill is-done"><span class="status-dot-mini"></span>Concluída</span>';
      } else if (status === 'EXPIRADO') {
        statusHtml = '<span class="status-pill is-expired"><span class="status-dot-mini"></span>Expirada</span>';
      } else {
        statusHtml = '<span class="status-pill is-pending"><span class="status-dot-mini"></span>Pendente</span>';
      }

      const isConcluido = status === 'CONCLUIDO';

      return `
        <tr>
          <td class="font-mono">#${t.id}</td>
          <td>
            <strong>${escapeHtml(t.titulo)}</strong>
            ${t.descricao ? `<div style="font-size: 11.5px; color: var(--text-tertiary);">${escapeHtml(t.descricao)}</div>` : ''}
          </td>
          <td><span class="font-mono">Pet #${t.petId}</span></td>
          <td>${escapeHtml(t.usuarioNome || t.usuarioEmail || 'Sem atribuição')}</td>
          <td>${formatDate(t.prazo)}</td>
          <td>${statusHtml}</td>
          <td><strong style="font-variant-numeric: tabular-nums;">+${t.pontosTarefa || 0}</strong></td>
          <td class="text-right">
            <div class="action-buttons-wrap">
              <button type="button" class="btn-ghost-action btn-toggle-task" data-task-id="${t.id}" data-task-done="${isConcluido}" title="${isConcluido ? 'Desmarcar' : 'Concluir'}">
                ${isConcluido ? 'Desmarcar' : 'Concluir'}
              </button>
              <button type="button" class="btn-ghost-action btn-edit-task" data-task-id="${t.id}">
                Editar
              </button>
              <button type="button" class="btn-ghost-action btn-danger btn-delete-task" data-task-id="${t.id}">
                Excluir
              </button>
            </div>
          </td>
        </tr>
      `;
    }).join('');

    dom.tbodyTarefas.querySelectorAll('.btn-toggle-task').forEach(btn => {
      btn.addEventListener('click', async () => {
        const taskId = Number(btn.dataset.taskId);
        const isDone = btn.dataset.taskDone === 'true';
        await toggleTaskStatus(taskId, isDone);
      });
    });

    dom.tbodyTarefas.querySelectorAll('.btn-edit-task').forEach(btn => {
      btn.addEventListener('click', () => openTaskModal(Number(btn.dataset.taskId)));
    });

    dom.tbodyTarefas.querySelectorAll('.btn-delete-task').forEach(btn => {
      btn.addEventListener('click', () => confirmAndDeleteTask(Number(btn.dataset.taskId)));
    });
  }

  function openTaskModal(taskId = null) {
    dom.formTask.reset();
    populateSelectOptions();

    if (taskId) {
      const t = state.tarefas.find(x => x.id === taskId);
      if (!t) return;
      dom.taskModalTitle.textContent = `Editar Tarefa #${t.id}`;
      dom.taskId.value = t.id;
      dom.taskTitulo.value = t.titulo || '';
      dom.taskDescricao.value = t.descricao || '';
      dom.taskPetId.value = t.petId || '';
      dom.taskPontos.value = t.pontosTarefa ?? 5;
      dom.taskStatus.value = (t.status || 'PENDENTE').toUpperCase();

      if (t.prazo) {
        // Formato para input datetime-local: YYYY-MM-DDTHH:mm
        dom.taskPrazo.value = t.prazo.substring(0, 16);
      }
    } else {
      dom.taskModalTitle.textContent = 'Nova Tarefa';
      dom.taskId.value = '';
      // Prazo padrão: amanhã no mesmo horário
      const tomorrow = new Date(Date.now() + 86400000);
      dom.taskPrazo.value = tomorrow.toISOString().substring(0, 16);
      dom.taskStatus.value = 'PENDENTE';
    }
    dom.taskModal.classList.add('active');
  }

  async function saveTaskForm() {
    const id = dom.taskId.value ? Number(dom.taskId.value) : null;
    const titulo = dom.taskTitulo.value.trim();
    const descricao = dom.taskDescricao.value.trim();
    const petId = Number(dom.taskPetId.value);
    const prazoRaw = dom.taskPrazo.value;
    const pontosTarefa = Number(dom.taskPontos.value);
    const status = dom.taskStatus.value;

    if (!titulo || !descricao || !petId || !prazoRaw) {
      showToast('Preencha os campos obrigatórios da tarefa.', 'warning');
      return;
    }

    // Backend valida @Future (LocalDateTime)
    const prazoDate = new Date(prazoRaw);
    if (prazoDate.getTime() <= Date.now()) {
      showToast('O prazo da tarefa deve ser uma data e hora futura.', 'warning');
      return;
    }

    const payload = {
      titulo,
      descricao,
      petId,
      prazo: prazoRaw.length === 16 ? `${prazoRaw}:00` : prazoRaw,
      pontosTarefa,
      status
    };

    const btnSubmit = document.getElementById('btn-save-task');
    btnSubmit.disabled = true;

    try {
      if (id) {
        showToast(`Atualizando tarefa #${id}...`, 'info');
        await apiFetch(`/tarefas/${id}`, {
          method: 'PUT',
          body: JSON.stringify(payload)
        });
        showToast('Tarefa atualizada com sucesso!', 'success');
      } else {
        showToast('Criando nova tarefa...', 'info');
        await apiFetch('/tarefas', {
          method: 'POST',
          body: JSON.stringify(payload)
        });
        showToast('Tarefa criada com sucesso!', 'success');
      }

      dom.taskModal.classList.remove('active');
      await loadAllData();
    } catch (err) {
      showToast(err.message || 'Falha ao salvar tarefa.', 'error');
    } finally {
      btnSubmit.disabled = false;
    }
  }

  async function toggleTaskStatus(taskId, isCurrentlyDone) {
    try {
      const endpoint = isCurrentlyDone ? `/tarefas/${taskId}/desmarcar` : `/tarefas/${taskId}/concluir`;
      showToast(isCurrentlyDone ? 'Desmarcando tarefa...' : 'Concluindo tarefa...', 'info');
      await apiFetch(endpoint, { method: 'PATCH' });
      await loadAllData();
      showToast('Status da tarefa atualizado!', 'success');
    } catch (err) {
      showToast(err.message || 'Falha ao atualizar tarefa.', 'error');
    }
  }

  async function confirmAndDeleteTask(taskId) {
    const confirmed = await showConfirmDialog({
      title: 'Excluir Tarefa',
      message: `Tem certeza que deseja excluir a tarefa #${taskId}? Esta ação não poderá ser desfeita.`,
      confirmText: 'Sim, Excluir Tarefa',
      isDanger: true
    });
    if (!confirmed) return;

    try {
      showToast(`Excluindo tarefa #${taskId}...`, 'info');
      await apiFetch(`/tarefas/${taskId}`, { method: 'DELETE' });
      state.tarefas = state.tarefas.filter(t => t.id !== taskId);
      computeAndRenderMetrics();
      renderTasksTable();
      showToast('Tarefa excluída com sucesso.', 'success');
    } catch (err) {
      showToast(err.message || 'Falha ao excluir tarefa.', 'error');
    }
  }

  // =========================================================================
  // 11. GESTÃO DE TRILHAS, MÓDULOS, AULAS & CONTEÚDOS NOSQL (MONGODB)
  // =========================================================================

  function renderTrilhasTables() {
    renderTrilhasSubTable();
    renderModulosSubTable();
    renderAulasSubTable();
    renderConteudosSubTable();
  }

  // A. Trilhas
  function renderTrilhasSubTable() {
    if (!dom.tbodyTrilhas) return;
    const q = state.trilhaSearch.toLowerCase();
    const filtered = state.trilhas.filter(t => !q || (t.nome || '').toLowerCase().includes(q));

    if (filtered.length === 0) {
      dom.tbodyTrilhas.innerHTML = `<tr><td colspan="5" class="table-cell-empty">Nenhuma trilha cadastrada.</td></tr>`;
      return;
    }

    dom.tbodyTrilhas.innerHTML = filtered.map(t => `
      <tr>
        <td class="font-mono">#${t.id}</td>
        <td><strong>${escapeHtml(t.nome)}</strong></td>
        <td><small class="text-secondary">${escapeHtml(t.descricao || '-')}</small></td>
        <td><span class="font-mono">Pet #${t.petId}</span></td>
        <td class="text-right">
          <div class="action-buttons-wrap">
            <button type="button" class="btn-ghost-action btn-edit-trilha" data-id="${t.id}">Editar</button>
            <button type="button" class="btn-ghost-action btn-danger btn-delete-trilha" data-id="${t.id}">Excluir</button>
          </div>
        </td>
      </tr>
    `).join('');

    dom.tbodyTrilhas.querySelectorAll('.btn-edit-trilha').forEach(b => {
      b.addEventListener('click', () => openTrilhaModal(Number(b.dataset.id)));
    });
    dom.tbodyTrilhas.querySelectorAll('.btn-delete-trilha').forEach(b => {
      b.addEventListener('click', () => deleteTrilha(Number(b.dataset.id)));
    });
  }

  function openTrilhaModal(trilhaId = null) {
    dom.formTrilha.reset();
    populateSelectOptions();

    if (trilhaId) {
      const t = state.trilhas.find(x => x.id === trilhaId);
      if (!t) return;
      dom.trilhaModalTitle.textContent = `Editar Trilha #${t.id}`;
      dom.trilhaId.value = t.id;
      dom.trilhaNome.value = t.nome || '';
      dom.trilhaDescricao.value = t.descricao || '';
      dom.trilhaPetId.value = t.petId || '';
    } else {
      dom.trilhaModalTitle.textContent = 'Nova Trilha';
      dom.trilhaId.value = '';
    }
    dom.trilhaModal.classList.add('active');
  }

  async function saveTrilhaForm() {
    const id = dom.trilhaId.value ? Number(dom.trilhaId.value) : null;
    const nome = dom.trilhaNome.value.trim();
    const descricao = dom.trilhaDescricao.value.trim();
    const petId = Number(dom.trilhaPetId.value);

    if (!nome || !descricao || !petId) {
      showToast('Preencha todos os campos da trilha.', 'warning');
      return;
    }

    const payload = { nome, descricao, petId };
    const btnSubmit = document.getElementById('btn-save-trilha');
    btnSubmit.disabled = true;

    try {
      if (id) {
        showToast(`Atualizando trilha #${id}...`, 'info');
        await apiFetch(`/trilhas/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
        showToast('Trilha atualizada!', 'success');
      } else {
        showToast('Criando trilha...', 'info');
        await apiFetch('/trilhas', { method: 'POST', body: JSON.stringify(payload) });
        showToast('Trilha cadastrada com sucesso!', 'success');
      }
      dom.trilhaModal.classList.remove('active');
      await loadAllData();
    } catch (err) {
      showToast(err.message || 'Falha ao salvar trilha.', 'error');
    } finally {
      btnSubmit.disabled = false;
    }
  }

  async function deleteTrilha(id) {
    const confirmed = await showConfirmDialog({
      title: 'Excluir Trilha',
      message: `Tem certeza que deseja excluir a Trilha #${id}? Certifique-se de que não haja dependências ativas.`,
      confirmText: 'Sim, Excluir Trilha',
      isDanger: true
    });
    if (!confirmed) return;
    try {
      showToast(`Excluindo trilha #${id}...`, 'info');
      await apiFetch(`/trilhas/${id}`, { method: 'DELETE' });
      await loadAllData();
      showToast('Trilha excluída com sucesso.', 'success');
    } catch (err) {
      showToast(err.message || 'Falha ao excluir trilha.', 'error');
    }
  }

  // B. Módulos
  function renderModulosSubTable() {
    if (!dom.tbodyModulos) return;
    const q = state.moduloSearch.toLowerCase();
    const filtered = state.modulos.filter(m => !q || (m.nome || '').toLowerCase().includes(q));

    if (filtered.length === 0) {
      dom.tbodyModulos.innerHTML = `<tr><td colspan="6" class="table-cell-empty">Nenhum módulo cadastrado.</td></tr>`;
      return;
    }

    dom.tbodyModulos.innerHTML = filtered.map(m => `
      <tr>
        <td class="font-mono">#${m.id}</td>
        <td><strong>${escapeHtml(m.nome)}</strong></td>
        <td><span class="font-mono">Trilha #${m.trilhaId}</span></td>
        <td><span class="badge-subtle">${escapeHtml(m.tempoConclusao || '30 min')}</span></td>
        <td><small class="text-secondary">${escapeHtml(m.descricao || '-')}</small></td>
        <td class="text-right">
          <div class="action-buttons-wrap">
            <button type="button" class="btn-ghost-action btn-edit-modulo" data-id="${m.id}">Editar</button>
            <button type="button" class="btn-ghost-action btn-danger btn-delete-modulo" data-id="${m.id}">Excluir</button>
          </div>
        </td>
      </tr>
    `).join('');

    dom.tbodyModulos.querySelectorAll('.btn-edit-modulo').forEach(b => {
      b.addEventListener('click', () => openModuloModal(Number(b.dataset.id)));
    });
    dom.tbodyModulos.querySelectorAll('.btn-delete-modulo').forEach(b => {
      b.addEventListener('click', () => deleteModulo(Number(b.dataset.id)));
    });
  }

  function openModuloModal(moduloId = null) {
    dom.formModulo.reset();
    populateSelectOptions();

    if (moduloId) {
      const m = state.modulos.find(x => x.id === moduloId);
      if (!m) return;
      dom.moduloModalTitle.textContent = `Editar Módulo #${m.id}`;
      dom.moduloId.value = m.id;
      dom.moduloTrilhaId.value = m.trilhaId || '';
      dom.moduloNome.value = m.nome || '';
      dom.moduloTempo.value = m.tempoConclusao || '30 min';
      dom.moduloDescricao.value = m.descricao || '';
    } else {
      dom.moduloModalTitle.textContent = 'Novo Módulo';
      dom.moduloId.value = '';
      dom.moduloTempo.value = '30 min';
    }
    dom.moduloModal.classList.add('active');
  }

  async function saveModuloForm() {
    const id = dom.moduloId.value ? Number(dom.moduloId.value) : null;
    const trilhaId = Number(dom.moduloTrilhaId.value);
    const nome = dom.moduloNome.value.trim();
    const tempoConclusao = dom.moduloTempo.value.trim();
    const descricao = dom.moduloDescricao.value.trim();

    if (!trilhaId || !nome || !tempoConclusao || !descricao) {
      showToast('Preencha todos os campos do módulo.', 'warning');
      return;
    }

    const payload = { trilhaId, nome, tempoConclusao, descricao };
    const btnSubmit = document.getElementById('btn-save-modulo');
    btnSubmit.disabled = true;

    try {
      if (id) {
        showToast(`Atualizando módulo #${id}...`, 'info');
        await apiFetch(`/modulos/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
        showToast('Módulo atualizado com sucesso!', 'success');
      } else {
        showToast('Criando módulo...', 'info');
        await apiFetch('/modulos', { method: 'POST', body: JSON.stringify(payload) });
        showToast('Módulo cadastrado!', 'success');
      }
      dom.moduloModal.classList.remove('active');
      await loadAllData();
    } catch (err) {
      showToast(err.message || 'Falha ao salvar módulo.', 'error');
    } finally {
      btnSubmit.disabled = false;
    }
  }

  async function deleteModulo(id) {
    const confirmed = await showConfirmDialog({
      title: 'Excluir Módulo',
      message: `Tem certeza que deseja excluir o Módulo #${id}?`,
      confirmText: 'Sim, Excluir Módulo',
      isDanger: true
    });
    if (!confirmed) return;
    try {
      showToast(`Excluindo módulo #${id}...`, 'info');
      await apiFetch(`/modulos/${id}`, { method: 'DELETE' });
      await loadAllData();
      showToast('Módulo excluído com sucesso.', 'success');
    } catch (err) {
      showToast(err.message || 'Falha ao excluir módulo.', 'error');
    }
  }

  // C. Aulas
  function renderAulasSubTable() {
    if (!dom.tbodyAulas) return;
    const q = state.aulaSearch.toLowerCase();
    const filtered = state.aulas.filter(a => !q || (a.nome || '').toLowerCase().includes(q));

    if (filtered.length === 0) {
      dom.tbodyAulas.innerHTML = `<tr><td colspan="7" class="table-cell-empty">Nenhuma aula cadastrada.</td></tr>`;
      return;
    }

    dom.tbodyAulas.innerHTML = filtered.map(a => {
      const dif = (a.dificuldade || 'MEDIO').toUpperCase();
      const difClass = dif === 'FACIL' ? 'dificuldade-facil' : (dif === 'AVANCADO' ? 'dificuldade-avancado' : 'dificuldade-medio');
      const isConcluida = a.concluida === true;

      return `
        <tr>
          <td class="font-mono">#${a.id}</td>
          <td><strong>${escapeHtml(a.nome)}</strong></td>
          <td><span class="font-mono">Módulo #${a.moduloId}</span></td>
          <td><span class="badge-dificuldade ${difClass}">${dif}</span></td>
          <td><strong style="font-variant-numeric: tabular-nums;">+${a.pontosAula || 0}</strong></td>
          <td>
            ${isConcluida
              ? '<span class="status-pill is-done"><span class="status-dot-mini"></span>Concluída</span>'
              : '<span class="status-pill is-pending"><span class="status-dot-mini"></span>Em Aberto</span>'}
          </td>
          <td class="text-right">
            <div class="action-buttons-wrap">
              <button type="button" class="btn-ghost-action btn-edit-conteudo-aula" data-aula-id="${a.id}" title="Editor NoSQL Markdown">
                📝 NoSQL
              </button>
              <button type="button" class="btn-ghost-action btn-toggle-aula" data-id="${a.id}" data-done="${isConcluida}">
                ${isConcluida ? 'Desmarcar' : 'Concluir'}
              </button>
              <button type="button" class="btn-ghost-action btn-edit-aula" data-id="${a.id}">Editar</button>
              <button type="button" class="btn-ghost-action btn-danger btn-delete-aula" data-id="${a.id}">Excluir</button>
            </div>
          </td>
        </tr>
      `;
    }).join('');

    dom.tbodyAulas.querySelectorAll('.btn-edit-conteudo-aula').forEach(b => {
      b.addEventListener('click', () => openConteudoModal(Number(b.dataset.aulaId)));
    });
    dom.tbodyAulas.querySelectorAll('.btn-toggle-aula').forEach(b => {
      b.addEventListener('click', () => toggleAulaConcluida(Number(b.dataset.id), b.dataset.done === 'true'));
    });
    dom.tbodyAulas.querySelectorAll('.btn-edit-aula').forEach(b => {
      b.addEventListener('click', () => openAulaModal(Number(b.dataset.id)));
    });
    dom.tbodyAulas.querySelectorAll('.btn-delete-aula').forEach(b => {
      b.addEventListener('click', () => deleteAula(Number(b.dataset.id)));
    });
  }

  function openAulaModal(aulaId = null) {
    dom.formAula.reset();
    populateSelectOptions();

    if (aulaId) {
      const a = state.aulas.find(x => x.id === aulaId);
      if (!a) return;
      dom.aulaModalTitle.textContent = `Editar Aula #${a.id}`;
      dom.aulaId.value = a.id;
      dom.aulaModuloId.value = a.moduloId || '';
      dom.aulaNome.value = a.nome || '';
      dom.aulaDescricao.value = a.descricao || '';
      dom.aulaPontos.value = a.pontosAula ?? 5;
      dom.aulaDificuldade.value = (a.dificuldade || 'MEDIO').toUpperCase();
      dom.aulaConcluida.value = a.concluida ? 'true' : 'false';
    } else {
      dom.aulaModalTitle.textContent = 'Nova Aula';
      dom.aulaId.value = '';
      dom.aulaPontos.value = 5;
      dom.aulaDificuldade.value = 'MEDIO';
      dom.aulaConcluida.value = 'false';
    }
    dom.aulaModal.classList.add('active');
  }

  async function saveAulaForm() {
    const id = dom.aulaId.value ? Number(dom.aulaId.value) : null;
    const moduloId = Number(dom.aulaModuloId.value);
    const nome = dom.aulaNome.value.trim();
    const descricao = dom.aulaDescricao.value.trim();
    const pontosAula = Number(dom.aulaPontos.value);
    const dificuldade = dom.aulaDificuldade.value;
    const concluida = dom.aulaConcluida.value === 'true';

    if (!moduloId || !nome || !descricao || isNaN(pontosAula)) {
      showToast('Preencha os campos obrigatórios da aula.', 'warning');
      return;
    }

    const payload = {
      moduloId,
      nome,
      descricao,
      pontosAula,
      dificuldade,
      concluida
    };

    const btnSubmit = document.getElementById('btn-save-aula');
    btnSubmit.disabled = true;

    try {
      if (id) {
        showToast(`Atualizando aula #${id}...`, 'info');
        await apiFetch(`/aulas/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
        showToast('Aula atualizada com sucesso!', 'success');
      } else {
        showToast('Criando aula...', 'info');
        await apiFetch('/aulas', { method: 'POST', body: JSON.stringify(payload) });
        showToast('Aula cadastrada com sucesso!', 'success');
      }
      dom.aulaModal.classList.remove('active');
      await loadAllData();
    } catch (err) {
      showToast(err.message || 'Falha ao salvar aula.', 'error');
    } finally {
      btnSubmit.disabled = false;
    }
  }

  async function toggleAulaConcluida(id, isCurrentlyDone) {
    try {
      const endpoint = isCurrentlyDone ? `/aulas/${id}/desmarcar` : `/aulas/${id}/concluir`;
      showToast(isCurrentlyDone ? 'Desmarcando aula...' : 'Concluindo aula...', 'info');
      await apiFetch(endpoint, { method: 'PATCH' });
      await loadAllData();
      showToast('Status da aula atualizado!', 'success');
    } catch (err) {
      showToast(err.message || 'Falha ao atualizar aula.', 'error');
    }
  }

  async function deleteAula(id) {
    const confirmed = await showConfirmDialog({
      title: 'Excluir Aula',
      message: `Tem certeza que deseja excluir a Aula #${id}? O conteúdo NoSQL associado no MongoDB Atlas também poderá ser afetado.`,
      confirmText: 'Sim, Excluir Aula',
      isDanger: true
    });
    if (!confirmed) return;
    try {
      showToast(`Excluindo aula #${id}...`, 'info');
      await apiFetch(`/aulas/${id}`, { method: 'DELETE' });
      await loadAllData();
      showToast('Aula excluída com sucesso.', 'success');
    } catch (err) {
      showToast(err.message || 'Falha ao excluir aula.', 'error');
    }
  }

  // D. Conteúdos NoSQL MongoDB Atlas
  function renderConteudosSubTable() {
    if (!dom.tbodyConteudos) return;
    const q = state.conteudoSearch.toLowerCase();
    const filtered = state.conteudosMongo.filter(c => !q ||
      String(c.aulaId).includes(q) ||
      (c.corpoMarkdown || '').toLowerCase().includes(q) ||
      (c.tipoConteudo || '').toLowerCase().includes(q));

    if (filtered.length === 0) {
      dom.tbodyConteudos.innerHTML = `<tr><td colspan="6" class="table-cell-empty">Nenhum conteúdo rico no MongoDB Atlas.</td></tr>`;
      return;
    }

    dom.tbodyConteudos.innerHTML = filtered.map(c => {
      const snippet = (c.corpoMarkdown || '').slice(0, 60).replace(/\n/g, ' ') + '...';
      const qtdLinks = (c.linksRecursos || []).length;

      return `
        <tr>
          <td><strong class="font-mono">Aula #${c.aulaId}</strong></td>
          <td><span class="badge-subtle">${escapeHtml(c.tipoConteudo || 'TEXTO_MARKDOWN')}</span></td>
          <td><small class="text-secondary font-mono">${escapeHtml(snippet)}</small></td>
          <td>${qtdLinks} links</td>
          <td>${formatDate(c.atualizadoEm)}</td>
          <td class="text-right">
            <div class="action-buttons-wrap">
              <button type="button" class="btn-ghost-action btn-edit-conteudo" data-aula-id="${c.aulaId}">
                Editor Markdown
              </button>
              <button type="button" class="btn-ghost-action btn-danger btn-delete-conteudo" data-aula-id="${c.aulaId}">
                Excluir
              </button>
            </div>
          </td>
        </tr>
      `;
    }).join('');

    dom.tbodyConteudos.querySelectorAll('.btn-edit-conteudo').forEach(b => {
      b.addEventListener('click', () => openConteudoModal(Number(b.dataset.aulaId)));
    });
    dom.tbodyConteudos.querySelectorAll('.btn-delete-conteudo').forEach(b => {
      b.addEventListener('click', () => deleteConteudo(Number(b.dataset.aulaId)));
    });
  }

  async function openConteudoModal(aulaId = null) {
    dom.formConteudo.reset();
    dom.conteudoPreview.innerHTML = '<p class="text-tertiary">Digite no editor à esquerda para visualizar a prévia formatada.</p>';

    if (!aulaId) {
      const selectedAulaId = prompt('Informe o ID da Aula à qual vincular o conteúdo NoSQL:');
      if (!selectedAulaId) return;
      aulaId = Number(selectedAulaId);
    }

    dom.conteudoAulaId.value = aulaId;
    dom.conteudoModalSubtitle.textContent = `Vinculado à Aula #${aulaId} • Armazenado no MongoDB Atlas`;

    // Tenta carregar conteúdo existente
    try {
      const existing = await apiFetch(`/aulas/${aulaId}/conteudo`);
      if (existing) {
        dom.conteudoTipo.value = existing.tipoConteudo || 'TEXTO_MARKDOWN';
        dom.conteudoLinks.value = (existing.linksRecursos || []).join(', ');
        dom.conteudoMarkdown.value = existing.corpoMarkdown || '';
        dom.conteudoPreview.innerHTML = renderSimpleMarkdown(existing.corpoMarkdown);
        dom.btnDeleteConteudo.classList.remove('hidden');
      }
    } catch {
      // Se 404, abre em modo de criação
      dom.conteudoTipo.value = 'TEXTO_MARKDOWN';
      dom.conteudoLinks.value = '';
      dom.conteudoMarkdown.value = `# Conteúdo Didático da Aula #${aulaId}\n\nEscreva as orientações práticas em Markdown aqui...\n\n### Passos de Adestramento:\n1. Primeiro passo com pet\n2. Segundo passo\n\n> Dica: utilize reforço positivo!`;
      dom.conteudoPreview.innerHTML = renderSimpleMarkdown(dom.conteudoMarkdown.value);
      dom.btnDeleteConteudo.classList.add('hidden');
    }

    dom.conteudoModal.classList.add('active');
  }

  async function saveConteudoForm() {
    const aulaId = Number(dom.conteudoAulaId.value);
    const tipoConteudo = dom.conteudoTipo.value.trim();
    const linksRaw = dom.conteudoLinks.value.trim();
    const corpoMarkdown = dom.conteudoMarkdown.value.trim();

    if (!aulaId || !tipoConteudo || !corpoMarkdown) {
      showToast('Preencha os campos obrigatórios do conteúdo.', 'warning');
      return;
    }

    const linksRecursos = linksRaw
      ? linksRaw.split(',').map(s => s.trim()).filter(Boolean)
      : [];

    const payload = {
      tipoConteudo,
      corpoMarkdown,
      linksRecursos
    };

    const btnSubmit = document.getElementById('btn-save-conteudo');
    btnSubmit.disabled = true;

    try {
      showToast('Salvando no MongoDB Atlas...', 'info');
      // Tenta criar ou atualizar (PUT ou POST)
      try {
        await apiFetch(`/aulas/${aulaId}/conteudo`, {
          method: 'PUT',
          body: JSON.stringify(payload)
        });
      } catch {
        await apiFetch(`/aulas/${aulaId}/conteudo`, {
          method: 'POST',
          body: JSON.stringify(payload)
        });
      }

      showToast('Conteúdo NoSQL salvo com sucesso!', 'success');
      dom.conteudoModal.classList.remove('active');
      await loadAllData();
    } catch (err) {
      showToast(err.message || 'Falha ao salvar conteúdo no MongoDB.', 'error');
    } finally {
      btnSubmit.disabled = false;
    }
  }

  async function deleteConteudo(aulaId) {
    const confirmed = await showConfirmDialog({
      title: 'Excluir Conteúdo NoSQL',
      message: `Tem certeza que deseja excluir definitivamente o documento de conteúdo da Aula #${aulaId} no MongoDB Atlas?`,
      confirmText: 'Sim, Excluir do MongoDB',
      isDanger: true
    });
    if (!confirmed) return;

    try {
      showToast(`Excluindo conteúdo da Aula #${aulaId}...`, 'info');
      await apiFetch(`/aulas/${aulaId}/conteudo`, { method: 'DELETE' });
      dom.conteudoModal.classList.remove('active');
      await loadAllData();
      showToast('Conteúdo NoSQL excluído.', 'success');
    } catch (err) {
      showToast(err.message || 'Falha ao excluir conteúdo.', 'error');
    }
  }

  // =========================================================================
  // 12. MODAL DA REDE DE CUIDADO (CARE CIRCLE & STORED PROCEDURES)
  // =========================================================================

  async function openCareCircleModal(userId) {
    dom.careCircleContent.innerHTML = `
      <div style="text-align: center; padding: 24px; color: var(--text-tertiary);">
        Carregando dados da rede de cuidado...
      </div>
    `;
    dom.careCircleModal.classList.add('active');

    try {
      const data = await apiFetch(`/usuarios/${userId}/rede-cuidado`);
      renderCareCircleDetails(data);
    } catch (err) {
      dom.careCircleContent.innerHTML = `
        <div style="padding: 16px; color: var(--rose);">
          Falha ao consultar rede de cuidado: ${escapeHtml(err.message)}
        </div>
      `;
    }
  }

  function renderCareCircleDetails(data) {
    dom.careCircleTitle.textContent = data.nomeUsuario || 'Tutor';
    dom.careCircleSubtitle.textContent = `${data.emailUsuario} • Classificação: ${data.nivelGamificacao || 'INICIANTE'}`;

    const pets = data.pets || [];
    const coCuidadores = data.coCuidadores || [];

    dom.careCircleContent.innerHTML = `
      <div class="care-stats-row">
        <div class="care-stat-block">
          <span class="care-stat-val">${data.pontosAcumulados || 0}</span>
          <span class="care-stat-lbl">Pontos Totais</span>
        </div>
        <div class="care-stat-block">
          <span class="care-stat-val">${data.totalTarefasConcluidas || 0}</span>
          <span class="care-stat-lbl">Concluídas</span>
        </div>
        <div class="care-stat-block">
          <span class="care-stat-val">${data.totalTarefasPendentes || 0}</span>
          <span class="care-stat-lbl">Pendentes</span>
        </div>
        <div class="care-stat-block">
          <span class="care-stat-tag">${escapeHtml(data.nivelGamificacao || 'BRONZE')}</span>
          <span class="care-stat-lbl">Nível</span>
        </div>
      </div>

      <div class="care-group-box">
        <div class="care-group-title">Pets Vinculados (${pets.length})</div>
        ${pets.length === 0 ? '<p style="color: var(--text-tertiary); font-size: 12.5px;">Nenhum animal vinculado.</p>' : `
          <div>
            ${pets.map(p => `
              <div class="care-item-row">
                <div>
                  <div class="care-item-name">${escapeHtml(p.nome)} <small style="color: var(--text-tertiary);">(${escapeHtml(p.raca || 'SRD')})</small></div>
                  <div class="care-item-sub">${p.responsavelPrincipal ? 'Responsável Principal' : 'Co-cuidador'}</div>
                </div>
                <div style="font-size: 12px; color: var(--text-secondary); font-variant-numeric: tabular-nums;">
                  ${p.totalTarefas || 0} tarefas
                </div>
              </div>
            `).join('')}
          </div>
        `}
      </div>

      <div class="care-group-box">
        <div class="care-group-title">Co-cuidadores Associados (${coCuidadores.length})</div>
        ${coCuidadores.length === 0 ? '<p style="color: var(--text-tertiary); font-size: 12.5px;">Nenhum co-cuidador registrado.</p>' : `
          <div>
            ${coCuidadores.map(c => `
              <div class="care-item-row">
                <div>
                  <div class="care-item-name">${escapeHtml(c.nome)}</div>
                  <div class="care-item-sub">${escapeHtml(c.email)}</div>
                </div>
                <div style="font-size: 12px; color: var(--text-tertiary);">
                  ${(c.petNomes || []).map(escapeHtml).join(', ') || 'Nenhum'}
                </div>
              </div>
            `).join('')}
          </div>
        `}
      </div>
    `;
  }

  // =========================================================================
  // 13. NAVEGAÇÃO & ATUALIZAÇÃO AUTOMÁTICA
  // =========================================================================

  function setupNavigation() {
    const navButtons = document.querySelectorAll('.sidebar-nav .nav-button');
    const tabPanes = document.querySelectorAll('.tab-pane');

    const viewTitles = {
      'tab-dashboard': {
        title: 'Métricas Globais',
        subtitle: 'Painel consolidado em tempo real'
      },
      'tab-usuarios': {
        title: 'Gestão de Usuários & Acessos (CRM)',
        subtitle: 'Controle completo de dados, perfis (RBAC) e localização'
      },
      'tab-pets': {
        title: 'Pets & Pacientes Monitorados',
        subtitle: 'Ficha biométrica, porte, castração e histórico'
      },
      'tab-tarefas': {
        title: 'Rotina de Cuidados & Tarefas',
        subtitle: 'Acompanhamento preventivo, prazos e gamificação'
      },
      'tab-trilhas': {
        title: 'Trilhas, Módulos, Aulas & NoSQL',
        subtitle: 'Gerenciamento pedagógico relacional e conteúdos ricos no MongoDB Atlas'
      },
      'tab-infra': {
        title: 'Serviços & Infraestrutura',
        subtitle: 'Conectividade e diagnóstico da topologia corporativa'
      }
    };

    navButtons.forEach(btn => {
      btn.addEventListener('click', () => {
        const targetTab = btn.dataset.tab;
        if (!targetTab) return;

        navButtons.forEach(b => b.classList.remove('active'));
        tabPanes.forEach(p => p.classList.remove('active'));

        btn.classList.add('active');
        const activePane = document.getElementById(targetTab);
        if (activePane) activePane.classList.add('active');

        state.activeTab = targetTab;

        if (viewTitles[targetTab]) {
          dom.pageTitle.textContent = viewTitles[targetTab].title;
          dom.pageSubtitle.textContent = viewTitles[targetTab].subtitle;
        }
      });
    });

    // Sub-abas de Trilhas
    const subTabButtons = document.querySelectorAll('.trilha-view-switcher .btn-sub-tab');
    const subTabPanes = document.querySelectorAll('.subtab-pane');

    subTabButtons.forEach(btn => {
      btn.addEventListener('click', () => {
        const targetSub = btn.dataset.subtab;
        if (!targetSub) return;

        subTabButtons.forEach(b => b.classList.remove('active'));
        subTabPanes.forEach(p => p.classList.remove('active'));

        btn.classList.add('active');
        const activeSubPane = document.getElementById(targetSub);
        if (activeSubPane) activeSubPane.classList.add('active');

        state.activeSubTab = targetSub;
      });
    });
  }

  function setupAutoRefresh() {
    if (state.refreshIntervalId) {
      clearInterval(state.refreshIntervalId);
      state.refreshIntervalId = null;
    }

    const intervalMs = Number(dom.selectRefresh.value);
    if (intervalMs > 0) {
      state.refreshIntervalId = setInterval(() => {
        if (state.token) {
          loadAllData();
        }
      }, intervalMs);
    }
  }

  // =========================================================================
  // 14. INICIALIZAÇÃO E OUVINTES GLOBAIS
  // =========================================================================

  function init() {
    function syncEnvironment(url, triggerReload = false) {
      state.apiUrl = url;
      localStorage.setItem('pg_admin_api_url', url);
      if (dom.apiUrlInput) dom.apiUrlInput.value = url;
      if (dom.selectEnvTopbar) dom.selectEnvTopbar.value = url;
      if (dom.btnEnvAzure) {
        dom.btnEnvAzure.classList.toggle('active', url.includes('azurewebsites.net'));
      }
      if (dom.btnEnvLocalhost) {
        dom.btnEnvLocalhost.classList.toggle('active', url.includes('localhost'));
      }
      if (triggerReload && state.token) {
        loadAllData();
      }
    }

    // Inicializa seleção e endpoints de ambiente
    syncEnvironment(state.apiUrl, false);

    if (dom.btnEnvAzure) {
      dom.btnEnvAzure.addEventListener('click', () => {
        syncEnvironment('https://app-petguardian-01bd2f0c.azurewebsites.net', false);
        showToast('Ambiente definido: Azure Cloud (Padrão)', 'info');
      });
    }

    if (dom.btnEnvLocalhost) {
      dom.btnEnvLocalhost.addEventListener('click', () => {
        syncEnvironment('http://localhost:8080', false);
        showToast('Ambiente definido: Localhost (8080)', 'info');
      });
    }

    if (dom.selectEnvTopbar) {
      dom.selectEnvTopbar.addEventListener('change', (e) => {
        const selected = e.target.value;
        syncEnvironment(selected, true);
        showToast(`Ambiente alterado para: ${selected}`, 'info');
      });
    }

    if (dom.apiUrlInput) {
      dom.apiUrlInput.addEventListener('change', (e) => {
        const typedUrl = e.target.value.trim();
        if (typedUrl) {
          syncEnvironment(typedUrl, false);
        }
      });
    }

    // Ouvintes do Modal de Confirmação Executiva
    if (dom.btnConfirmAction) {
      dom.btnConfirmAction.addEventListener('click', () => closeConfirmDialog(true));
    }
    if (dom.btnConfirmCancel) {
      dom.btnConfirmCancel.addEventListener('click', () => closeConfirmDialog(false));
    }
    if (dom.btnCloseConfirm) {
      dom.btnCloseConfirm.addEventListener('click', () => closeConfirmDialog(false));
    }

    // Revelar / Ocultar Senha com Ícone de Olho
    if (dom.btnToggleUserSenha && dom.userSenha) {
      dom.btnToggleUserSenha.addEventListener('click', () => {
        const isPassword = dom.userSenha.type === 'password';
        dom.userSenha.type = isPassword ? 'text' : 'password';
        const showIcon = dom.btnToggleUserSenha.querySelector('.eye-icon-show');
        const hideIcon = dom.btnToggleUserSenha.querySelector('.eye-icon-hide');
        if (showIcon && hideIcon) {
          if (isPassword) {
            showIcon.classList.add('hidden');
            hideIcon.classList.remove('hidden');
          } else {
            showIcon.classList.remove('hidden');
            hideIcon.classList.add('hidden');
          }
        }
      });
    }

    // Login e Ping
    if (dom.loginForm) {
      dom.loginForm.addEventListener('submit', (e) => {
        e.preventDefault();
        handleLogin();
      });
    }
    if (dom.btnSubmitLogin) dom.btnSubmitLogin.addEventListener('click', handleLogin);
    if (dom.btnFillAdmin) {
      dom.btnFillAdmin.addEventListener('click', () => {
        dom.loginEmail.value = 'enzo.admin@petguardian.com';
        dom.loginPassword.value = '123mudar';
        showToast('Credenciais preenchidas.', 'info');
      });
    }
    if (dom.btnPingApi) dom.btnPingApi.addEventListener('click', pingApi);
    if (dom.btnLogout) dom.btnLogout.addEventListener('click', handleLogout);

    // Refresh
    if (dom.btnRefreshAll) {
      dom.btnRefreshAll.addEventListener('click', () => {
        loadAllData();
        showToast('Atualizando dados...', 'info');
      });
    }
    if (dom.selectRefresh) dom.selectRefresh.addEventListener('change', setupAutoRefresh);

    // Fechamento genérico de modais
    document.querySelectorAll('.modal-close-trigger').forEach(btn => {
      btn.addEventListener('click', (e) => {
        const modal = e.target.closest('.modal-overlay');
        if (modal) modal.classList.remove('active');
      });
    });
    if (dom.btnCloseCareCircle) {
      dom.btnCloseCareCircle.addEventListener('click', () => dom.careCircleModal.classList.remove('active'));
    }

    document.querySelectorAll('.modal-overlay').forEach(overlay => {
      overlay.addEventListener('click', (e) => {
        if (e.target === overlay && overlay.id !== 'login-modal') {
          overlay.classList.remove('active');
        }
      });
    });

    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape') {
        document.querySelectorAll('.modal-overlay.active').forEach(m => {
          if (m.id !== 'login-modal') m.classList.remove('active');
        });
      }
    });

    // Abertura de Modais de Criação
    if (dom.btnOpenNewUser) dom.btnOpenNewUser.addEventListener('click', () => openUserModal(null));
    if (dom.btnOpenNewPet) dom.btnOpenNewPet.addEventListener('click', () => openPetModal(null));
    if (dom.btnOpenNewTask) dom.btnOpenNewTask.addEventListener('click', () => openTaskModal(null));
    if (dom.btnOpenNewTrilha) dom.btnOpenNewTrilha.addEventListener('click', () => openTrilhaModal(null));
    if (dom.btnOpenNewModulo) dom.btnOpenNewModulo.addEventListener('click', () => openModuloModal(null));
    if (dom.btnOpenNewAula) dom.btnOpenNewAula.addEventListener('click', () => openAulaModal(null));
    if (dom.btnOpenNewConteudo) dom.btnOpenNewConteudo.addEventListener('click', () => openConteudoModal(null));

    // Submissão dos Formulários
    if (dom.formUser) dom.formUser.addEventListener('submit', (e) => { e.preventDefault(); saveUserForm(); });
    if (dom.formPet) dom.formPet.addEventListener('submit', (e) => { e.preventDefault(); savePetForm(); });
    if (dom.formTask) dom.formTask.addEventListener('submit', (e) => { e.preventDefault(); saveTaskForm(); });
    if (dom.formTrilha) dom.formTrilha.addEventListener('submit', (e) => { e.preventDefault(); saveTrilhaForm(); });
    if (dom.formModulo) dom.formModulo.addEventListener('submit', (e) => { e.preventDefault(); saveModuloForm(); });
    if (dom.formAula) dom.formAula.addEventListener('submit', (e) => { e.preventDefault(); saveAulaForm(); });
    if (dom.formConteudo) dom.formConteudo.addEventListener('submit', (e) => { e.preventDefault(); saveConteudoForm(); });
    if (dom.btnDeleteConteudo) {
      dom.btnDeleteConteudo.addEventListener('click', () => {
        const aulaId = Number(dom.conteudoAulaId.value);
        if (aulaId) deleteConteudo(aulaId);
      });
    }

    // Live Preview no Editor de Markdown
    if (dom.conteudoMarkdown) {
      dom.conteudoMarkdown.addEventListener('input', (e) => {
        dom.conteudoPreview.innerHTML = renderSimpleMarkdown(e.target.value);
      });
    }

    // Consulta ViaCEP dinâmica
    if (dom.userCep) {
      dom.userCep.addEventListener('input', (e) => {
        const val = e.target.value.replace(/\D/g, '');
        if (val.length === 8) {
          lookupViaCep(val);
        }
      });
    }


    // Buscas e Filtros
    if (dom.inputSearchUser) {
      dom.inputSearchUser.addEventListener('input', (e) => {
        state.userSearch = e.target.value.trim();
        renderUsersTable();
      });
    }
    document.querySelectorAll('#tab-usuarios .btn-filter-pill').forEach(btn => {
      btn.addEventListener('click', () => {
        document.querySelectorAll('#tab-usuarios .btn-filter-pill').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        state.userRoleFilter = btn.dataset.role || 'ALL';
        renderUsersTable();
      });
    });

    if (dom.inputSearchPet) {
      dom.inputSearchPet.addEventListener('input', (e) => {
        state.petSearch = e.target.value.trim();
        renderPetsTable();
      });
    }

    if (dom.inputSearchTask) {
      dom.inputSearchTask.addEventListener('input', (e) => {
        state.taskSearch = e.target.value.trim();
        renderTasksTable();
      });
    }
    document.querySelectorAll('#tab-tarefas .btn-filter-pill').forEach(btn => {
      btn.addEventListener('click', () => {
        document.querySelectorAll('#tab-tarefas .btn-filter-pill').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        state.taskStatusFilter = btn.dataset.status || 'ALL';
        renderTasksTable();
      });
    });

    if (dom.inputSearchTrilha) {
      dom.inputSearchTrilha.addEventListener('input', (e) => {
        state.trilhaSearch = e.target.value.trim();
        renderTrilhasSubTable();
      });
    }
    if (dom.inputSearchModulo) {
      dom.inputSearchModulo.addEventListener('input', (e) => {
        state.moduloSearch = e.target.value.trim();
        renderModulosSubTable();
      });
    }
    if (dom.inputSearchAula) {
      dom.inputSearchAula.addEventListener('input', (e) => {
        state.aulaSearch = e.target.value.trim();
        renderAulasSubTable();
      });
    }
    if (dom.inputSearchConteudo) {
      dom.inputSearchConteudo.addEventListener('input', (e) => {
        state.conteudoSearch = e.target.value.trim();
        renderConteudosSubTable();
      });
    }

    setupNavigation();

    // Verificação de Sessão Ativa
    if (state.token && state.user && (state.user.role || '').toUpperCase() === 'ADMIN') {
      dom.loginModal.classList.remove('active');
      dom.appContainer.classList.remove('hidden');
      renderAdminProfile();
      loadAllData();
      setupAutoRefresh();
    } else {
      dom.loginModal.classList.add('active');
      dom.appContainer.classList.add('hidden');
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
