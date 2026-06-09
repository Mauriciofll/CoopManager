const STORAGE_KEY = "coopmanager-mobile-state-v1";

const seedState = {
  dark: false,
  view: "summary",
  role: "admin",
  setupComplete: false,
  admin: null,
  nextClientId: 1,
  nextProducerId: 1,
  nextOrderId: 1,
  nextProductId: 1,
  clients: [],
  producers: [],
  products: [],
  orders: []
};

let state = loadState();

const app = document.querySelector("#app");
const screenTitle = document.querySelector("#screenTitle");
const themeButton = document.querySelector("#themeButton");
const profileStrip = document.querySelector(".profile-strip");
const bottomNav = document.querySelector(".bottom-nav");

document.addEventListener("click", (event) => {
  const nav = event.target.closest("[data-view]");
  if (nav) {
    state.view = nav.dataset.view;
    saveState();
    render();
    return;
  }

  const role = event.target.closest("[data-role]");
  if (role) {
    state.role = role.dataset.role;
    saveState();
    render();
  }
});

themeButton.addEventListener("click", () => {
  state.dark = !state.dark;
  saveState();
  applyTheme();
});

if ("serviceWorker" in navigator) {
  window.addEventListener("load", () => {
    navigator.serviceWorker.register("sw.js").catch(() => {});
  });
}

applyTheme();
render();

function loadState() {
  try {
    const saved = JSON.parse(localStorage.getItem(STORAGE_KEY));
    return saved?.setupComplete ? { ...cloneSeed(), ...saved } : cloneSeed();
  } catch {
    return cloneSeed();
  }
}

function cloneSeed() {
  return JSON.parse(JSON.stringify(seedState));
}

function saveState() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
}

function applyTheme() {
  document.documentElement.classList.toggle("dark", state.dark);
}

function render() {
  if (!state.setupComplete) {
    screenTitle.textContent = "Primeiro acesso";
    profileStrip.hidden = true;
    bottomNav.hidden = true;
    app.innerHTML = "";
    app.append(renderSetup());
    app.focus({ preventScroll: true });
    return;
  }

  profileStrip.hidden = false;
  bottomNav.hidden = false;
  renderRoleStrip();
  document.querySelectorAll(".nav-item").forEach((button) => {
    button.classList.toggle("is-active", button.dataset.view === state.view);
  });
  document.querySelectorAll(".role-chip").forEach((button) => {
    button.classList.toggle("is-active", button.dataset.role === state.role);
  });

  const renderers = {
    summary: renderSummary,
    orders: renderOrders,
    products: renderProducts,
    lists: renderLists
  };
  const titles = {
    summary: "Resumo",
    orders: "Pedidos",
    products: "Produtos",
    lists: "Listas"
  };

  screenTitle.textContent = titles[state.view] || "Resumo";
  app.innerHTML = "";
  app.append(...renderers[state.view]());
  app.focus({ preventScroll: true });
}

function renderSetup() {
  const name = input("text", "");
  const login = input("text", "");
  const password = input("password", "");
  const confirm = input("password", "");
  const button = el("button", "primary", "Criar administrador");
  button.type = "button";
  button.addEventListener("click", () => {
    if (!name.value.trim() || !login.value.trim() || !password.value) {
      toast(button, "Preencha os campos");
      return;
    }
    if (password.value !== confirm.value) {
      toast(button, "Senha diferente");
      confirm.value = "";
      confirm.focus();
      return;
    }

    state.admin = {
      name: name.value.trim(),
      login: login.value.trim()
    };
    state.setupComplete = true;
    state.role = "admin";
    saveState();
    render();
  });

  return card("Configuração inicial", [
    el("p", "muted", "Crie o primeiro usuário para usar o CoopManager."),
    el("div", "form", [
      field("Nome", name),
      field("Login", login),
      field("Senha", password),
      field("Confirmar senha", confirm),
      button
    ])
  ]);
}

function renderRoleStrip() {
  if (!state.producers.some((producer) => `producer-${producer.id}` === state.role) && state.role !== "admin") {
    state.role = "admin";
  }

  profileStrip.innerHTML = "";
  profileStrip.append(roleButton("Intermediador", "admin"));
  state.producers.forEach((producer) => {
    profileStrip.append(roleButton(producer.short || producer.name, `producer-${producer.id}`));
  });
}

function roleButton(text, role) {
  const button = el("button", `role-chip ${state.role === role ? "is-active" : ""}`, text);
  button.type = "button";
  button.dataset.role = role;
  return button;
}

function renderSummary() {
  const orders = visibleOrders();
  const products = visibleProducts();
  const activeOrders = orders.filter(isActiveOrder);
  const dueToday = activeOrders.filter((order) => order.due === todayIso()).length;
  const late = activeOrders.filter((order) => order.due && order.due < todayIso()).length;
  const divergences = activeOrders.reduce((total, order) => total + order.items.filter(hasDivergence).length, 0);
  const lowStock = products.filter((product) => product.stock <= product.minimum).length;

  const metrics = el("section", "metric-grid", [
    metric("Produtos", products.length),
    metric("Pedidos ativos", activeOrders.length),
    metric("Hoje", dueToday),
    metric("Atrasados", late),
    metric("Divergências", divergences),
    metric("Baixo estoque", lowStock)
  ]);

  const alerts = activeOrders.flatMap((order) => order.items.map((item) => alertForItem(order, item)).filter(Boolean));
  const alertCard = card("Alertas", alerts.length
    ? alerts.slice(0, 6).map((text) => el("div", "item-line", text))
    : [emptyState("Sem alertas operacionais agora.")]);

  const nextOrders = activeOrders.slice(0, 4).map(orderCardCompact);
  const ordersCard = card("Próximos pedidos", nextOrders.length ? nextOrders : [emptyState("Nenhum pedido ativo.")]);

  return [metrics, alertCard, ordersCard];
}

function renderOrders() {
  const nodes = [];
  if (isAdmin()) {
    nodes.push(renderClientForm());
    nodes.push(renderNewOrderForm());
  }

  const orders = visibleOrders();
  nodes.push(...orders.map(orderCard));
  if (orders.length === 0) {
    nodes.push(emptyState("Nenhum pedido para este perfil."));
  }
  return nodes;
}

function renderProducts() {
  const nodes = [];
  if (isAdmin()) {
    nodes.push(renderProducerForm());
  }
  if (!isAdmin()) {
    nodes.push(renderProductForm());
  }

  const products = visibleProducts();
  nodes.push(...products.map((product) => card("", [
    el("div", "card-header", [
      el("div", "", [
        el("h3", "", product.name),
        el("div", "muted small", producerName(product.producerId))
      ]),
      el("span", `pill ${product.stock <= product.minimum ? "warning" : ""}`, money(product.price))
    ]),
    el("div", "row", [
      el("span", "muted", product.category),
      el("strong", "", `Estoque ${product.stock} | mínimo ${product.minimum}`)
    ])
  ])));
  if (products.length === 0) {
    nodes.push(emptyState("Nenhum produto para este perfil."));
  }
  return nodes;
}

function renderLists() {
  let current = "producer";
  const output = el("textarea", "", "");
  output.readOnly = true;

  const segmented = el("div", "segmented", [
    chip("Produtor", "producer"),
    chip("Conferência", "receiving"),
    chip("Separação", "packing")
  ]);

  const updateOutput = () => {
    output.value = buildList(current);
    segmented.querySelectorAll("button").forEach((button) => {
      button.classList.toggle("is-active", button.dataset.list === current);
    });
  };

  segmented.addEventListener("click", (event) => {
    const button = event.target.closest("[data-list]");
    if (!button) return;
    current = button.dataset.list;
    updateOutput();
  });

  const copy = el("button", "primary", "Copiar lista");
  copy.type = "button";
  copy.addEventListener("click", async () => {
    try {
      await navigator.clipboard.writeText(output.value);
      toast(copy, "Copiada");
    } catch {
      output.select();
      document.execCommand("copy");
      toast(copy, "Copiada");
    }
  });

  updateOutput();
  return [card("Listas operacionais", [segmented, output, copy])];
}

function renderNewOrderForm() {
  if (state.clients.length === 0 || state.products.length === 0) {
    return card("Novo pedido", [
      emptyState("Cadastre ao menos um cliente, um produtor e um produto antes de registrar pedidos.")
    ]);
  }

  const clientSelect = select(state.clients.map((client) => [client.id, client.name]));
  const productSelect = select(state.products.map((product) => [product.id, `${product.name} | ${producerName(product.producerId)}`]));
  const quantity = input("number", "1");
  quantity.min = "1";
  const due = input("date", todayIso());
  const type = select([["RETIRADA", "Retirada"], ["ENTREGA", "Entrega"]]);

  const button = el("button", "primary", "Registrar pedido");
  button.type = "button";
  button.addEventListener("click", () => {
    const product = findProduct(Number(productSelect.value));
    const amount = Math.max(1, Number(quantity.value || 1));
    if (!product || amount > product.stock) {
      toast(button, "Estoque insuficiente");
      return;
    }
    product.stock -= amount;
    state.orders.unshift({
      id: state.nextOrderId++,
      clientId: Number(clientSelect.value),
      type: type.value,
      due: due.value,
      status: "AGUARDANDO_PRODUTORES",
      items: [{ productId: product.id, quantity: amount, sent: 0, received: 0, status: "PENDENTE" }],
      history: ["Pedido criado no mobile."]
    });
    saveState();
    render();
  });

  return card("Novo pedido", [
    el("div", "form", [
      field("Cliente", clientSelect),
      field("Produto", productSelect),
      field("Quantidade", quantity),
      field("Data prevista", due),
      field("Entrega", type),
      button
    ])
  ]);
}

function renderClientForm() {
  const name = input("text", "");
  const phone = input("tel", "");
  const button = el("button", "secondary", "Cadastrar cliente");
  button.type = "button";
  button.addEventListener("click", () => {
    if (!name.value.trim()) {
      toast(button, "Informe o nome");
      return;
    }
    state.clients.push({
      id: state.nextClientId++,
      name: name.value.trim(),
      phone: phone.value.trim()
    });
    saveState();
    render();
  });

  return card("Cliente", [
    el("div", "form", [
      field("Nome", name),
      field("Telefone", phone),
      button
    ])
  ]);
}

function renderProducerForm() {
  const name = input("text", "");
  const short = input("text", "");
  const button = el("button", "secondary", "Cadastrar produtor");
  button.type = "button";
  button.addEventListener("click", () => {
    if (!name.value.trim()) {
      toast(button, "Informe o nome");
      return;
    }
    const producer = {
      id: state.nextProducerId++,
      name: name.value.trim(),
      short: short.value.trim() || name.value.trim()
    };
    state.producers.push(producer);
    state.role = `producer-${producer.id}`;
    saveState();
    render();
  });

  return card("Produtor", [
    el("div", "form", [
      field("Nome", name),
      field("Nome curto", short),
      button
    ])
  ]);
}

function renderProductForm() {
  const name = input("text", "");
  const price = input("number", "10");
  price.step = "0.01";
  const stock = input("number", "1");
  const minimum = input("number", "1");
  const category = select([["AGRICULTURA", "Agricultura"], ["ARTESANATO", "Artesanato"]]);

  const button = el("button", "primary", "Cadastrar produto");
  button.type = "button";
  button.addEventListener("click", () => {
    if (!name.value.trim()) {
      toast(button, "Informe o nome");
      return;
    }
    state.products.push({
      id: state.nextProductId++,
      producerId: currentProducerId(),
      name: name.value.trim(),
      category: category.value,
      price: Math.max(0.01, Number(price.value || 0)),
      stock: Math.max(0, Number(stock.value || 0)),
      minimum: Math.max(0, Number(minimum.value || 0))
    });
    saveState();
    render();
  });

  return card("Produto do produtor", [
    el("div", "form", [
      field("Nome", name),
      field("Categoria", category),
      field("Preço", price),
      field("Estoque", stock),
      field("Mínimo", minimum),
      button
    ])
  ]);
}

function orderCard(order) {
  const items = order.items.map((item) => orderItem(order, item));
  return card("", [
    el("div", "card-header", [
      el("div", "", [
        el("h3", "", `Pedido #${order.id}`),
        el("div", "muted small", `${clientName(order.clientId)} | ${dateText(order.due)} | ${order.type}`)
      ]),
      el("span", `pill ${orderPillClass(order)}`, statusText(order.status))
    ]),
    ...items
  ]);
}

function orderCardCompact(order) {
  return el("div", "item-line", [
    el("strong", "", `Pedido #${order.id} | ${clientName(order.clientId)}`),
    el("span", "muted", `${dateText(order.due)} | ${statusText(order.status)}`)
  ]);
}

function orderItem(order, item) {
  const product = findProduct(item.productId);
  const title = `${product ? product.name : "Produto removido"} x${item.quantity}`;
  const flow = `Enviado ${item.sent}/${item.quantity} | Recebido ${item.received}/${item.quantity}`;
  const actions = itemActions(order, item);
  return el("div", "item-line", [
    el("strong", "", title),
    el("span", "muted", `${producerName(product?.producerId)} | ${flow}`),
    actions
  ]);
}

function itemActions(order, item) {
  const product = findProduct(item.productId);
  if (!isActiveOrder(order) || !product) {
    return el("div", "muted small", statusText(item.status));
  }

  if (!isAdmin() && product.producerId === currentProducerId() && item.sent < item.quantity) {
    return actionGroup([
      actionButton("Enviar tudo", () => moveItem(order, item, "send-all")),
      actionButton("Parcial", () => partialMove(order, item, "send"))
    ]);
  }

  if (isAdmin() && item.sent > item.received) {
    return actionGroup([
      actionButton("Receber tudo", () => moveItem(order, item, "receive-all")),
      actionButton("Parcial", () => partialMove(order, item, "receive"))
    ]);
  }

  if (hasDivergence(item)) {
    return el("div", "pill danger", "Investigar diferença");
  }
  return el("div", "muted small", nextActionText(item));
}

function actionGroup(buttons) {
  return el("div", "actions", buttons);
}

function actionButton(text, handler) {
  const button = el("button", text.includes("Parcial") ? "secondary" : "primary", text);
  button.type = "button";
  button.addEventListener("click", handler);
  return button;
}

function partialMove(order, item, type) {
  const max = type === "send"
    ? item.quantity - item.sent - 1
    : item.sent - item.received - 1;
  if (max < 1) return;
  const value = Number(prompt(`Quantidade parcial (1 a ${max})`, "1"));
  if (!Number.isFinite(value) || value < 1 || value > max) return;
  if (type === "send") {
    item.sent += value;
    item.status = "ENVIADO";
    order.history.push(`Envio parcial: ${value} unidade(s).`);
  } else {
    item.received += value;
    item.status = item.received >= item.quantity ? "ENTREGUE_COOPERATIVA" : "ENVIADO";
    order.history.push(`Recebimento parcial: ${value} unidade(s).`);
  }
  recalcOrder(order);
  saveState();
  render();
}

function moveItem(order, item, action) {
  if (action === "send-all") {
    item.sent = item.quantity;
    item.status = "ENVIADO";
    order.history.push("Envio total registrado.");
  }
  if (action === "receive-all") {
    item.received = item.sent;
    item.status = item.received >= item.quantity ? "ENTREGUE_COOPERATIVA" : "ENVIADO";
    order.history.push("Recebimento total do que foi enviado.");
  }
  recalcOrder(order);
  saveState();
  render();
}

function recalcOrder(order) {
  const items = order.items;
  if (items.some((item) => item.status === "INDISPONIVEL")) {
    order.status = "PENDENCIA";
  } else if (items.every((item) => item.received >= item.quantity)) {
    order.status = "EM_SEPARACAO";
  } else if (items.some((item) => item.received > 0)) {
    order.status = "PARCIALMENTE_RECEBIDO";
  } else if (items.some((item) => item.sent > 0)) {
    order.status = "AGUARDANDO_RECEBIMENTO";
  } else {
    order.status = "AGUARDANDO_PRODUTORES";
  }
}

function buildList(kind) {
  const orders = visibleOrders().filter(isActiveOrder);
  const lines = [];
  const title = {
    producer: "LISTA DO PRODUTOR",
    receiving: "CONFERÊNCIA NA COOPERATIVA",
    packing: "SEPARAÇÃO POR CLIENTE"
  }[kind];
  lines.push(title, "");

  orders.forEach((order) => {
    order.items.forEach((item) => {
      const product = findProduct(item.productId);
      if (!product) return;
      if (kind === "producer" && item.sent < item.quantity) {
        lines.push(`Pedido #${order.id} | ${clientName(order.clientId)} | ${producerName(product.producerId)}`);
        lines.push(`- ${product.name}: enviar ${item.quantity - item.sent} de ${item.quantity}`);
      }
      if (kind === "receiving" && item.sent > item.received) {
        lines.push(`Pedido #${order.id} | ${clientName(order.clientId)} | ${producerName(product.producerId)}`);
        lines.push(`- ${product.name}: conferir ${item.sent - item.received} | enviado ${item.sent}/${item.quantity}`);
      }
      if (kind === "packing") {
        const missing = item.quantity - item.received;
        lines.push(`Pedido #${order.id} | ${clientName(order.clientId)} | ${order.type}`);
        lines.push(missing <= 0
          ? `- ${product.name}: separar ${item.quantity}`
          : `- ${product.name}: aguardar ${missing} | recebido ${item.received}/${item.quantity}`);
      }
    });
    if (kind === "packing") lines.push("");
  });

  if (lines.length <= 2) {
    lines.push("Nenhuma pendência nos filtros atuais.");
  }
  return lines.join("\n");
}

function visibleProducts() {
  if (isAdmin()) return state.products;
  return state.products.filter((product) => product.producerId === currentProducerId());
}

function visibleOrders() {
  if (isAdmin()) return state.orders;
  return state.orders.filter((order) => order.items.some((item) => {
    const product = findProduct(item.productId);
    return product?.producerId === currentProducerId();
  }));
}

function alertForItem(order, item) {
  const product = findProduct(item.productId);
  if (!product || !isActiveOrder(order)) return "";
  if (hasDivergence(item)) return `Investigar diferença | Pedido #${order.id} | ${product.name}`;
  if (item.sent > item.received) return `Conferir chegada | Pedido #${order.id} | ${product.name}`;
  if (item.sent < item.quantity) return `Aguardar envio | Pedido #${order.id} | ${product.name}`;
  return "";
}

function nextActionText(item) {
  if (item.received >= item.quantity) return "Conferido";
  if (item.sent > item.received) return "Aguardando recebimento";
  if (item.sent < item.quantity) return "Aguardando produtor";
  return statusText(item.status);
}

function hasDivergence(item) {
  return item.received > 0 && item.sent > item.received && item.received < item.quantity;
}

function isAdmin() {
  return state.role === "admin";
}

function currentProducerId() {
  if (!state.role.startsWith("producer-")) {
    return 0;
  }
  return Number(state.role.replace("producer-", ""));
}

function isActiveOrder(order) {
  return order.status !== "ENTREGUE" && order.status !== "CANCELADO";
}

function findProduct(id) {
  return state.products.find((product) => product.id === Number(id));
}

function producerName(id) {
  if (!id) return "Sem produtor";
  return state.producers.find((producer) => producer.id === Number(id))?.name || "Sem produtor";
}

function clientName(id) {
  return state.clients.find((client) => client.id === Number(id))?.name || "Sem cliente";
}

function statusText(status) {
  const labels = {
    AGUARDANDO_PRODUTORES: "Aguardando produtores",
    AGUARDANDO_RECEBIMENTO: "Aguardando recebimento",
    PARCIALMENTE_RECEBIDO: "Parcialmente recebido",
    PENDENCIA: "Pendência",
    EM_SEPARACAO: "Em separação",
    PRONTO: "Pronto",
    ENTREGUE: "Entregue",
    CANCELADO: "Cancelado",
    ENVIADO: "Enviado",
    ENTREGUE_COOPERATIVA: "Recebido"
  };
  return labels[status] || status;
}

function orderPillClass(order) {
  if (order.due && order.due < todayIso() && isActiveOrder(order)) return "danger";
  if (order.due === todayIso() && isActiveOrder(order)) return "warning";
  return "";
}

function money(value) {
  return new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(value);
}

function dateText(value) {
  if (!value) return "Sem previsão";
  const [year, month, day] = value.split("-");
  return `${day}/${month}/${year}`;
}

function todayIso() {
  return localIsoDate(new Date());
}

function addDaysIso(days) {
  const date = new Date();
  date.setDate(date.getDate() + days);
  return localIsoDate(date);
}

function localIsoDate(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function metric(title, value) {
  return el("div", "card metric", [
    el("span", "muted small", title),
    el("strong", "", String(value))
  ]);
}

function card(title, children) {
  const content = Array.isArray(children) ? children : [children];
  return el("section", "card", [
    title ? el("h2", "", title) : null,
    ...content
  ].filter(Boolean));
}

function emptyState(text) {
  const template = document.querySelector("#emptyStateTemplate");
  const node = template.content.firstElementChild.cloneNode(true);
  node.querySelector("span").textContent = text;
  return node;
}

function field(labelText, control) {
  return el("label", "", [labelText, control]);
}

function input(type, value) {
  const node = document.createElement("input");
  node.type = type;
  node.value = value;
  return node;
}

function select(options) {
  const node = document.createElement("select");
  options.forEach(([value, text]) => {
    const option = document.createElement("option");
    option.value = value;
    option.textContent = text;
    node.append(option);
  });
  return node;
}

function chip(text, list) {
  const button = el("button", "chip", text);
  button.type = "button";
  button.dataset.list = list;
  return button;
}

function toast(button, text) {
  const original = button.textContent;
  button.textContent = text;
  button.disabled = true;
  setTimeout(() => {
    button.textContent = original;
    button.disabled = false;
  }, 900);
}

function el(tag, className, children = []) {
  const node = document.createElement(tag);
  if (className) node.className = className;
  if (!Array.isArray(children)) {
    node.textContent = children;
    return node;
  }
  children.forEach((child) => {
    if (child == null) return;
    if (typeof child === "string") {
      node.append(document.createTextNode(child));
    } else {
      node.append(child);
    }
  });
  return node;
}
