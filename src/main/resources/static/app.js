// URL base del backend de Spring Boot configurado en el puerto 8081
const API_BASE = '/api';

// Estado global de la aplicación (Usuario logueado)
let currentUserId = null;

// Elementos del DOM
const authSection = document.getElementById('auth-section');
const appSection = document.getElementById('app-section');
const feedContainer = document.getElementById('feed-container');

// --- 1. LÓGICA DE AUTENTICACIÓN ---
document.getElementById('login-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('login-username').value;
    const password = document.getElementById('login-password').value;
    const simulatedId = document.getElementById('login-userid').value;
    const msgEl = document.getElementById('login-message');

    try {
        const response = await fetch(`${API_BASE}/users/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        if (response.ok) {
            currentUserId = parseInt(simulatedId); // Guardamos el ID simulado
            authSection.classList.remove('active');
            appSection.classList.add('active');
            loadPosts(); // Cargamos el feed
        } else {
            const data = await response.json();
            msgEl.textContent = data.error || data.message || "Credenciales inválidas";
            msgEl.className = "message error";
        }
    } catch (error) {
        msgEl.textContent = "Error de red al conectar al servidor.";
        msgEl.className = "message error";
    }
});

document.getElementById('register-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('reg-username').value;
    const email = document.getElementById('reg-email').value;
    const password = document.getElementById('reg-password').value;
    const msgEl = document.getElementById('reg-message');

    try {
        const response = await fetch(`${API_BASE}/users`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, email, password })
        });

        if (response.ok) {
            msgEl.textContent = "Usuario registrado exitosamente. Ahora inicia sesión.";
            msgEl.className = "message success";
            document.getElementById('register-form').reset();
        } else {
            const data = await response.json();
            msgEl.textContent = data.error || "Error al registrar usuario";
            msgEl.className = "message error";
        }
    } catch (error) {
        msgEl.textContent = "Error de conexión.";
        msgEl.className = "message error";
    }
});

document.getElementById('logout-btn').addEventListener('click', () => {
    currentUserId = null;
    appSection.classList.remove('active');
    authSection.classList.add('active');
});

// --- 2. LÓGICA DE PUBLICACIONES (FEED) ---
document.getElementById('post-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const content = document.getElementById('post-content').value;
    const hashtagsRaw = document.getElementById('post-hashtags').value;
    
    // Convertir el string de hashtags en un array (ej. "#hola, #mundo" -> ["#hola", "#mundo"])
    const hashtags = hashtagsRaw ? hashtagsRaw.split(',').map(tag => tag.trim()) : [];

    try {
        const response = await fetch(`${API_BASE}/posts`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                authorId: currentUserId,
                content: content,
                mediaURLs: [],
                hashtags: hashtags
            })
        });

        if (response.ok) {
            document.getElementById('post-form').reset();
            loadPosts(); // Recargar los posts después de publicar
        } else {
            alert("Error al crear la publicación.");
        }
    } catch (error) {
        alert("Error de conexión al crear el post.");
    }
});

async function loadPosts() {
    feedContainer.innerHTML = '<p class="loading">Cargando publicaciones...</p>';
    
    try {
        const response = await fetch(`${API_BASE}/posts`);
        if (response.ok) {
            const data = await response.json();
            renderPosts(data.posts);
        } else {
            feedContainer.innerHTML = '<p class="error">Error al cargar el feed.</p>';
        }
    } catch (error) {
        feedContainer.innerHTML = '<p class="error">Error de red. No se pudo conectar al backend.</p>';
    }
}

// --- 3. PARSEO DEL XML DE eXist-db ---
function renderPosts(postsXmlArray) {
    feedContainer.innerHTML = '';

    if (!postsXmlArray || postsXmlArray.length === 0) {
        feedContainer.innerHTML = '<p>No hay publicaciones todavía.</p>';
        return;
    }

    const parser = new DOMParser();

    // Invertimos el array para que los más nuevos salgan arriba (opcional)
    postsXmlArray.reverse().forEach(xmlString => {
        // Parsear el string XML que devuelve eXist-db
        const xmlDoc = parser.parseFromString(xmlString, "application/xml");
        
        // Extraer los nodos
        const id = xmlDoc.getElementsByTagName("id")[0]?.textContent || "Desconocido";
        const idAutor = xmlDoc.getElementsByTagName("idAutor")[0]?.textContent || "Desconocido";
        const contenido = xmlDoc.getElementsByTagName("contenido")[0]?.textContent || "";
        const fecha = xmlDoc.getElementsByTagName("fechaCreacion")[0]?.textContent || "";
        
        // Extraer hashtags
        const tagsNodos = xmlDoc.getElementsByTagName("tag");
        let tagsList = [];
        for(let i = 0; i < tagsNodos.length; i++) {
            tagsList.push(tagsNodos[i].textContent);
        }

        // Crear la tarjeta HTML
        const postCard = document.createElement('div');
        postCard.className = 'card post';
        postCard.innerHTML = `
            <div class="post-header">
                <span class="post-id">Autor ID: ${idAutor}</span>
                <span>${fecha}</span>
            </div>
            <div class="post-content">
                ${contenido}
            </div>
            <div class="post-footer">
                ${tagsList.join(' ')}
            </div>
        `;
        feedContainer.appendChild(postCard);
    });
}
function getXmlValue(xmlDoc, tagName) {
    // Busca primero sin prefijo
    let el = xmlDoc.getElementsByTagName(tagName)[0];
    // Si no lo encuentra, busca con el prefijo "p:" que usa eXist-db
    if (!el) el = xmlDoc.getElementsByTagName(`p:${tagName}`)[0];
    return el ? el.textContent : "";
}

async function loadPosts() {
    feedContainer.innerHTML = '<p class="loading">Cargando publicaciones...</p>';
    
    try {
        const response = await fetch(`${API_BASE}/posts`);
        
        if (!response.ok) {
            const errData = await response.json().catch(() => ({}));
            feedContainer.innerHTML = `<p class="message error">Error del servidor (${response.status}): ${errData.error || response.statusText}</p>`;
            return;
        }

        const data = await response.json();
        console.log("Posts recibidos del backend:", data); // Para depurar en consola F12
        renderPosts(data.posts);
    } catch (error) {
        console.error("Error de conexión:", error);
        feedContainer.innerHTML = '<p class="message error">Error de conexión al cargar publicaciones. Revisa la consola (F12).</p>';
    }
}

function renderPosts(postsXmlArray) {
    feedContainer.innerHTML = '';

    if (!postsXmlArray || postsXmlArray.length === 0) {
        feedContainer.innerHTML = '<p style="text-align: center; color: #777;">No hay publicaciones todavía.</p>';
        return;
    }

    const parser = new DOMParser();

    // Copiamos y damos vuelta para mostrar los más recientes arriba
    [...postsXmlArray].reverse().forEach(xmlString => {
        const xmlDoc = parser.parseFromString(xmlString, "application/xml");
        
        // Validar si hubo error al parsear el XML
        const parseError = xmlDoc.getElementsByTagName("parsererror")[0];
        if (parseError) {
            console.error("XML inválido:", xmlString);
            return;
        }

        const idAutor = getXmlValue(xmlDoc, "idAutor") || "Desconocido";
        const contenido = getXmlValue(xmlDoc, "contenido");
        const fecha = getXmlValue(xmlDoc, "fechaCreacion");
        
        // Tags
        let tagsList = [];
        const tags = xmlDoc.querySelectorAll("tag, p\\:tag");
        tags.forEach(t => tagsList.push(t.textContent));

        const postCard = document.createElement('div');
        postCard.className = 'card post';
        postCard.innerHTML = `
            <div class="post-header">
                <span class="post-id">Autor ID: ${idAutor}</span>
                <span>${fecha}</span>
            </div>
            <div class="post-content">
                ${contenido}
            </div>
            <div class="post-footer">
                ${tagsList.map(t => `<span class="tag">${t}</span>`).join(' ')}
            </div>
        `;
        feedContainer.appendChild(postCard);
    });
}

// EJECUTAR AL CARGAR: Si la sección del feed está visible, carga los posts inmediatamente
window.addEventListener('DOMContentLoaded', () => {
    if (appSection.classList.contains('active')) {
        loadPosts();
    }
});
