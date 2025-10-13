// Minimal mock auth state for demo; integrate with your backend later
const state = {
	isAuthenticated: false,
	user: {
		login: "player_one",
		balance: 27.5,
		avatar: "https://images.unsplash.com/photo-1545996124-0501ebae84d0?q=80&w=200&auto=format&fit=crop"
	}
};

const $ = (sel) => document.querySelector(sel);

function updateAuthUI() {
	const guest = $("#authGuest");
	const profile = $("#authProfile");
	if (!guest || !profile) return;
	if (state.isAuthenticated) {
		guest.classList.add("hidden");
		profile.classList.remove("hidden");
		$("#profileName").textContent = state.user.login;
		$("#profileBalance").textContent = state.user.balance.toFixed(2);
		$("#profileAvatar").src = state.user.avatar;
	} else {
		guest.classList.remove("hidden");
		profile.classList.add("hidden");
	}
}

function animateCounters() {
	const counters = document.querySelectorAll(".stat__num");
	counters.forEach((el) => {
		const target = parseFloat(el.getAttribute("data-count") || "0");
		const isFloat = !Number.isInteger(target);
		let start = 0;
		const duration = 1000;
		const startTime = performance.now();
		function tick(now) {
			const progress = Math.min((now - startTime) / duration, 1);
			const value = start + (target - start) * progress;
			el.textContent = isFloat ? value.toFixed(1) : Math.round(value).toString();
			if (progress < 1) requestAnimationFrame(tick);
		}
		requestAnimationFrame(tick);
	});
}

function parallaxHero() {
	const img = document.querySelector(".hero__image");
	if (!img) return;
	let rafId = 0;
	function onMove(e) {
		if (rafId) cancelAnimationFrame(rafId);
		rafId = requestAnimationFrame(() => {
			const { innerWidth: w, innerHeight: h } = window;
			const x = (e.clientX - w / 2) / w;
			const y = (e.clientY - h / 2) / h;
			img.style.transform = `perspective(1200px) rotateY(${x * -8}deg) rotateX(${y * 6}deg) translateY(${y * -6}px)`;
		});
	}
	document.addEventListener("mousemove", onMove);
}

function setupTabs() {
	const tabButtons = document.querySelectorAll('.tabs .tab');
	const panes = document.querySelectorAll('.tabpane');
	if (!tabButtons.length) return;

	function activate(id) {
		panes.forEach(p => p.classList.toggle('active', p.id === id));
		tabButtons.forEach(b => {
			const selected = b.getAttribute('aria-controls') === id;
			b.classList.toggle('active', selected);
			b.setAttribute('aria-selected', selected ? 'true' : 'false');
		});
	}

	tabButtons.forEach(btn => {
		btn.addEventListener('click', () => activate(btn.getAttribute('aria-controls')));
	});
}

function wireUI() {
	const year = $("#year");
	if (year) year.textContent = new Date().getFullYear().toString();

	const btnLogin = $("#btnLogin");
	const btnRegister = $("#btnRegister");
	const btnLogout = $("#btnLogout");
	const btnBook = $("#btnBook");
	const btnCta = $("#btnCta");

	btnLogin?.addEventListener("click", () => {
		state.isAuthenticated = true;
		updateAuthUI();
	});

	btnRegister?.addEventListener("click", () => {
		state.isAuthenticated = true;
		state.user.balance += 5; // welcome bonus
		updateAuthUI();
	});

	btnLogout?.addEventListener("click", () => {
		state.isAuthenticated = false;
		updateAuthUI();
	});

	function pulse(el) {
		if (!el) return;
		el.style.transform = "scale(0.98)";
		setTimeout(() => (el.style.transform = ""), 100);
	}

	btnBook?.addEventListener("click", () => pulse(btnBook));
	btnCta?.addEventListener("click", () => pulse(btnCta));
}

window.addEventListener("DOMContentLoaded", () => {
	updateAuthUI();
	animateCounters();
	parallaxHero();
	setupTabs();
	wireUI();
});
