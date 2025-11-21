const aiResponseDiv = document.getElementById("aiResponse");
const aiPromptInput = document.getElementById("aiPrompt");

let chatHistory = JSON.parse(localStorage.getItem("chatHistory") || "[]");
chatHistory.forEach(msg => {
    const div = document.createElement("div");
    div.innerHTML = msg;
    aiResponseDiv.appendChild(div);
});

function scrollToBottom() {
    aiResponseDiv.scrollTop = aiResponseDiv.scrollHeight;
}
scrollToBottom();

//function askAI() {
//    const query = aiPromptInput.value.trim();
//    if (!query) return;
//
//	// 顯示用戶訊息
//	    const userDiv = document.createElement("div");
//	    userDiv.innerHTML = `<b>你:</b> ${query}`;
//	    aiResponseDiv.appendChild(userDiv);
//		
//		chatHistory.push(`<b>你:</b> ${query}`);
//	    localStorage.setItem("chatHistory", JSON.stringify(chatHistory));
//		
//		aiPromptInput.value = "";
//		scrollToBottom();
//		
//		// 呼叫後端 API
//		    fetch(`/api/ai/search?q=${encodeURIComponent(query)}`)
//		        .then(res => res.text())
//		        .then(answer => {
//		            const aiDiv = document.createElement("div");
//		            aiDiv.innerHTML = `<b>AI:</b> ${answer}`;
//		            aiResponseDiv.appendChild(aiDiv);
//
//		            chatHistory.push(`<b>AI:</b> ${answer}`);
//		            localStorage.setItem("chatHistory", JSON.stringify(chatHistory));
//
//		            scrollToBottom();
//		        })
//		        .catch(err => {
//		            console.error(err);
//		            const errDiv = document.createElement("div");
//		            errDiv.innerHTML = `<b>AI:</b> 服務暫時不可用`;
//		            aiResponseDiv.appendChild(errDiv);
//		        });
//		}
//		
//		// 支援 Enter 發送訊息
//		aiPromptInput.addEventListener("keydown", function(e) {
//		    if (e.key === "Enter" && !e.shiftKey) {
//		        e.preventDefault();
//		        askAI();
//		    }
//		});

async function askAI() {
    const query = document.getElementById("aiPrompt").value;
    const response = await fetch(`/api/ai/search?q=${encodeURIComponent(query)}`);
    if (!response.ok) {
        alert(`Error: ${response.status}`);
        return;
    }
    const answer = await response.text();
    document.getElementById("aiResponse").innerHTML = `<b>AI:</b> ${answer}`;
}
