// URL base del backend de Spring Boot configurado en el puerto 8081
const API_BASE = '/api';
let currentUserId = null;

const authSection = document.getElementById('auth-section');
const appSection = document.getElementById('app-section');
const feedContainer = document.getElementById('feed-container');

// Utilidad para extraer valores XML con o sin namespace (eXist-db usa p:etiqueta)
function getXmlValue(xmlDoc, tagName) {
    let el = xmlDoc.getElementsByTagName(tagName)[0];
    if (!el) el = xmlDoc.getElementsByTagName(`p:${tagName}`)[0];
    return el ? el.textContent : "";
}

// --- 1. AUTENTICACIÓN ---
document.getElementById('login-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('login-username').value;
    const password = document.getElementById('login-password').value;
    const msgEl = document.getElementById('login-message');

    try {
        const response = await fetch(`${API_BASE}/users/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();
        if (response.ok) {
            currentUserId = data.userId; // Captura el ID devuelto por el backend
            authSection.classList.remove('active');
            appSection.classList.add('active');
            loadPosts(); 
        } else {
            msgEl.textContent = data.error || data.message || "Credenciales inválidas";
            msgEl.className = "message error";
        }
    } catch (error) {
        msgEl.textContent = "Error de conexión.";
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
            msgEl.textContent = "Registro exitoso. Inicia sesión.";
            msgEl.className = "message success";
            document.getElementById('register-form').reset();
        } else {
            const data = await response.json();
            msgEl.textContent = data.error || "Error al registrar";
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

// --- 2. CREACIÓN DE POSTS ---
document.getElementById('post-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const content = document.getElementById('post-content').value;
    const hashtagsRaw = document.getElementById('post-hashtags').value;
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
            loadPosts();
        } else {
            alert("Error al crear la publicación.");
        }
    } catch (error) {
        alert("Error de conexión al crear el post.");
    }
});

// --- 3. CARGA Y RENDERIZADO DEL FEED ---
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
        feedContainer.innerHTML = '<p class="error">Error de red.</p>';
    }
}

function renderPosts(postsXmlArray) {
    feedContainer.innerHTML = '';
    if (!postsXmlArray || postsXmlArray.length === 0) {
        feedContainer.innerHTML = '<p style="text-align:center;">No hay publicaciones todavía.</p>';
        return;
    }

    const parser = new DOMParser();
    [...postsXmlArray].reverse().forEach(xmlString => {
        const xmlDoc = parser.parseFromString(xmlString, "application/xml");
        
        const idPost = getXmlValue(xmlDoc, "id");
        const idAutor = getXmlValue(xmlDoc, "idAutor");
        
        const nombreAutor = getXmlValue(xmlDoc, "nombreAutor") || `Usuario #${idAutor}`;
        const contenido = getXmlValue(xmlDoc, "contenido");
        const fecha = getXmlValue(xmlDoc, "fechaCreacion");
        
        let tagsList = [];
        xmlDoc.querySelectorAll("tag, p\\:tag").forEach(t => tagsList.push(t.textContent));

        // --- PROCESAR LIKES ---
        let likesArray = [];
        xmlDoc.querySelectorAll("like, p\\:like").forEach(l => likesArray.push(l.textContent));
        const likeCount = likesArray.length;
        const hasLiked = likesArray.includes(String(currentUserId));
        const heartIcon = hasLiked ? '❤' : '♡';

        // --- PROCESAR COMENTARIOS ---
        let commentsHtml = '';
        xmlDoc.querySelectorAll("comentario, p\\:comentario").forEach(c => {
            const cId = getXmlValue(c, "id");
            const cAutorId = getXmlValue(c, "idAutor");
            const cNombreAutor = getXmlValue(c, "nombreAutor") || `Usuario #${cAutorId}`;
            const cContenido = getXmlValue(c, "contenido");
            
            const canDeleteComment = (currentUserId == cAutorId || currentUserId == idAutor);
            const deleteBtnHtml = canDeleteComment ? `<button onclick="deleteComment(${idPost}, ${cId})" style="width:auto; padding:2px 8px; background:#dc3545; font-size:0.8em; color:white; border:none; border-radius:3px; cursor:pointer;">Borrar</button>` : '';

            commentsHtml += `
                <div style="background:#f4f6f8; padding:8px; margin-top:8px; border-radius:4px; display:flex; justify-content:space-between; align-items:center;">
                    <div>
                        <strong style="font-size:0.85em; color:#007bff;">${cNombreAutor}:</strong>
                        <span style="font-size:0.9em; margin-left:5px;">${cContenido}</span>
                    </div>
                    ${deleteBtnHtml}
                </div>
            `;
        });

        const postCard = document.createElement('div');
        postCard.className = 'card post';
        
        const deletePostBtn = (currentUserId == idAutor) ? `<button onclick="deletePost(${idPost})" style="width:auto; padding:2px 8px; background:#dc3545; margin-left:10px; color:white; border:none; border-radius:3px; cursor:pointer;">🗑️ Borrar Post</button>` : '';

        postCard.innerHTML = `
            <div class="post-header">
                <strong style="font-size:1.1em; color:#333;">${nombreAutor}</strong>
                <span>${fecha} ${deletePostBtn}</span>
            </div>
            <div class="post-content">${contenido}</div>
            <div class="post-footer">
                ${tagsList.map(t => `<span style="color:#007bff; font-weight:bold;">${t.startsWith('#') ? t : '#'+t}</span>`).join(' ')}
                
                <!-- SECCIÓN DE BOTÓN DE LIKE -->
                <div style="margin-top: 10px;">
                    <button onclick="toggleLike(${idPost}, ${hasLiked})" style="background:none; border:none; color:inherit; font-size:1.2em; padding:0; width:auto; cursor:pointer;">
                        ${heartIcon} <span style="font-size:0.9em; font-weight:normal;">${likeCount}</span>
                    </button>
                </div>
            </div>
            
            <hr style="margin:15px 0; border:0; border-top:1px solid #eee;">
            
            <div class="comments-section">
                <h4 style="margin-bottom:10px; font-size:1em;">Comentarios</h4>
                ${commentsHtml}
                <div style="display:flex; gap:10px; margin-top:15px;">
                    <input type="text" id="comment-input-${idPost}" placeholder="Escribe un comentario..." style="margin:0; flex:1; padding:8px; border:1px solid #ccc; border-radius:4px;">
                    <button onclick="addComment(${idPost})" style="width:auto; padding:5px 15px; background-color:#007bff; color:white; border:none; border-radius:4px; cursor:pointer;">Comentar</button>
                </div>
            </div>
        `;
        feedContainer.appendChild(postCard);
    });
}

// --- 4. FUNCIONES GLOBALES (Borrado e inserción de comentarios) ---
window.deletePost = async function(postId) {
    if(!confirm("¿Borrar esta publicación?")) return;
    await fetch(`${API_BASE}/posts/${postId}`, { method: 'DELETE' });
    loadPosts();
};

window.addComment = async function(postId) {
    const input = document.getElementById(`comment-input-${postId}`);
    if(!input.value.trim()) return;

    await fetch(`${API_BASE}/posts/${postId}/comments`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ authorId: currentUserId, content: input.value })
    });
    loadPosts();
};

window.deleteComment = async function(postId, commentId) {
    if(!confirm("¿Borrar este comentario?")) return;
    await fetch(`${API_BASE}/posts/${postId}/comments/${commentId}`, { method: 'DELETE' });
    loadPosts();
};

document.getElementById('delete-account-btn').addEventListener('click', async () => {
    if(!confirm("¿Seguro que deseas borrar TU CUENTA? Se eliminarán en cascada todos tus posts y comentarios.")) return;
    
    const response = await fetch(`${API_BASE}/users/${currentUserId}`, { method: 'DELETE' });
    if(response.ok) {
        document.getElementById('logout-btn').click(); 
        alert("Cuenta eliminada correctamente.");
    } else {
        alert("Error al intentar eliminar la cuenta.");
    }
});

// Función para dar/quitar Like
window.toggleLike = async function(postId, hasLiked) {
    const method = hasLiked ? 'DELETE' : 'POST';
    await fetch(`${API_BASE}/posts/${postId}/likes?userId=${currentUserId}`, { method: method });
    loadPosts(); 
};