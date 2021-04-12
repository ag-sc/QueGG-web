const AUTOCOMPLETE_CONFIG = {
    throttle: 500,
    interval_handle: null,
    latest_query: null,
    sent_latest: false,
    suppress_next: false,
}

function performQuery(qry) {
    console.log("[query]", qry);

    $.ajax({
        url: window.QUERY_URI,
        data: {'q': qry},
        type: "GET",
        cache: false,
    }).done(function (res) {
        callback(res.results)
    });
}

function setupAutoComplete(input) {

    document.querySelectorAll("form").forEach(form => {
        form.addEventListener("keydown", (evt) => {
            if (evt.which === 13) {

                evt.preventDefault();
            }
        });
    });

    function resetAutocomplete() {
        console.log("[autocomplete] reset"); // TODO
    }

    function populateResultList(results) {
        const targetList = document.querySelectorAll(".ac_suggestions")[0]

        if (!results) {
            targetList.innerHTML = "";
            return;
        }

        targetList.innerHTML = "";
        results.forEach((result, resultidx) => {
            let a = document.createElement('a');
            let nodeText = result.text;
            if (!result.leaf) {
                nodeText += "…"
            }
            if (result.entityOnset > -1 && result.entityOffset > -1) {
                let text_prefix = document.createElement("span");
                text_prefix.textContent = result.text.substring(0, result.entityOnset);
                a.appendChild(text_prefix);
                let text_entity = document.createElement("span");
                text_entity.textContent = result.text.substring(result.entityOnset, result.entityOffset);
                text_entity.classList.add("emarker");
                a.appendChild(text_entity);

                if (result.leaf) {
                    let text_suffix = document.createElement("span");
                    if (result.leaf) {
                        text_suffix.textContent = result.text.substring(result.entityOffset);
                    } else {
                        text_suffix.textContent = "…";
                    }
                    a.appendChild(text_suffix);
                }
            } else {
                let linkText = document.createTextNode(nodeText);
                a.appendChild(linkText);
            }
            a.dataset.suggestion = result.text;
            a.dataset.resultidx = resultidx;
            a.classList.add("list-group-item");
            if (resultidx === 0) {
                a.classList.add("active");
            }
            if (result.leaf) {
                a.classList.add("ac_leafnode");
            } else {
                a.classList.add("ac_branchnode");
            }

            a.addEventListener("keydown", (evt) => {
                if (evt.which === 13) {
                    evt.preventDefault();
                    return false;
                }
            });
            a.addEventListener("click", (evt) => {
                getSuggestions().forEach(elem => elem.classList.remove("active"));
                evt.target.classList.add("active");
                input.focus();
                updateActiveSuggestion();
                if (AUTOCOMPLETE_CONFIG.latest_query !== input.value) {
                   AUTOCOMPLETE_CONFIG.latest_query = input.value;
                   AUTOCOMPLETE_CONFIG.sent_latest = false;
               }
            });
            targetList.appendChild(a);
        });
        updateActiveSuggestion(true);
    }

    async function renderAnswer(answerDiv, answer) {
        console.log("[render]", answer);

        if (answer.constructor == Object) {
            // meta answer
            if (answer.result_type) {
                let valid_card = false;
                const answerContainer = document.createElement("div");
                answerContainer.classList.add("card");
                answerContainer.classList.add("col-4");

                if (answer['ethumbnail']) {
                    const tnail = document.createElement("img");
                    tnail.classList.add("card-img-top");
                    tnail.src = answer['ethumbnail'];
                    answerContainer.appendChild(tnail);
                    valid_card = true;
                }

                const answerBody = document.createElement("div");
                answerBody.classList.add("card-body");

                if (answer['elabel']) {
                    let elabel = answer['elabel'];

                    if (elabel.indexOf("@") > -1) {
                        elabel = elabel.substring(0, elabel.indexOf("@"));
                    }
                    if (elabel.indexOf("^^") > -1) {
                        elabel = elabel.substring(0, elabel.indexOf("^^"));
                    }

                    const cardTitle = document.createElement("h5");
                    cardTitle.classList.add("card-title");
                    cardTitle.textContent = elabel;
                    answerBody.appendChild(cardTitle);
                    valid_card = true;

                    let etype = answer["etype"];
                    if (etype) {
                        const etypeElem = document.createElement("div");
                        etypeElem.classList.add("ac_etype");
                        etypeElem.textContent = etype;
                        answerContainer.appendChild(etypeElem);
                    }
                }

                if (answer['eabstract']) {
                    const abstract = document.createElement("p");
                    abstract.classList.add("card-text");
                    abstract.textContent = answer['eabstract'].replaceAll("\\\"", "\"").substring(0, 100) + "..."
                    answerBody.appendChild(abstract);
                    valid_card = true;
                }

                if (answer['elink']) {
                    const cardlink = document.createElement("a");
                    cardlink.classList.add("btn");
                    cardlink.classList.add("btn-sm");
                    cardlink.classList.add("btn-light");
                    cardlink.href = answer['elink'];
                    cardlink.textContent = "more";
                    answerBody.appendChild(cardlink);
                    valid_card = true;
                }
                answerContainer.appendChild(answerBody);
                if (valid_card) {
                    answerDiv.appendChild(answerContainer);
                }
            } else {
                const answerContainer = document.createElement("div");
                answerContainer.classList.add("ac_literal_answer");

                let answerContent = "";
                for (const key of Object.keys(answer)) {
                    let value = answer[key];

                    let line = "";
                    if (key.length > 1) {
                        line = key + " ";
                    }

                    if (value.indexOf("@") > -1) {
                        value = value.substring(0, value.indexOf("@"));
                    }
                    if (value.indexOf("^^") > -1) {
                        value = value.substring(0, value.indexOf("^^"));
                    }

                    line += value;
                    answerContent += line + "\n";
                }
                var answerText = document.createTextNode(answerContent.replace("http://dbpedia.org/resource/", ""));
                answerContainer.appendChild(answerText);

                answerDiv.appendChild(answerContainer);
            }
        } else {
            const answerContainer = document.createElement("div");
            var answerText = document.createTextNode(answer);
            answerContainer.appendChild(answerText);
            answerDiv.appendChild(answerContainer);
        }
    }

    async function fetchAnswer(for_question, update_input) {
        console.log(for_question);
        if (!for_question) { return; }
        if (!for_question.leaf) { return; }

        const query_uri = new URL(window.location.protocol + "//" + window.location.host + window.QUERY_URI + "query");
        query_uri.searchParams.append("q", for_question.text);
        query_uri.searchParams.append("answer", "yes");

        let res = null;

        const answerDiv = document.getElementById("answer");
        try {
            document.getElementById("answer-spinner").style.display = "block";
            res = await fetch(query_uri, {
                method: "GET",
                mode: "cors",
                cache: "no-cache",

            });
        } catch (err) {
            console.error(err);
            answerDiv.innerHTML = err;
            document.getElementById("answer-spinner").style.display = "none";
            return;
        }
        document.getElementById("answer-spinner").style.display = "none";

        document.querySelectorAll(".qalabel").forEach(elem => {
            elem.style.display = "flex";
        });

        const res_data = await res.json();

        answerDiv.innerHTML = "";

        // TODO separate function to render answer data

        // res_data.sparql
        if (res_data['sparql-error']) {
            const errorMessage = document.createElement("div");
            errorMessage.classList.add("alert");
            errorMessage.classList.add("alert-warning");
            errorMessage.dataset['mdb-color'] = 'warning';

            var errorText = document.createTextNode(res_data['sparql-error']);
            errorMessage.appendChild(errorText);

            answerDiv.appendChild(errorMessage);
        }

        document.getElementById("question").textContent = res_data['question'];

        if (res_data['sparql-result']) {
            console.log("[render sparql result]");
            res_data['sparql-result'].forEach(async (answer) => {
                await renderAnswer(answerDiv, answer);
            });
        }

        if (res_data['sparql']) {
            document.getElementById("sparql-toggle").style.display = "block";
            document.getElementById("sparql").textContent = res_data['sparql'];
        }
    }

    async function runQuery() {
        AUTOCOMPLETE_CONFIG.suppress_next = false;
        if (AUTOCOMPLETE_CONFIG.sent_latest) { return; }
        if (AUTOCOMPLETE_CONFIG.latest_query === null) { return; }
        if (AUTOCOMPLETE_CONFIG.latest_query === "") {
            resetAutocomplete();
        }
        console.log("[query-run]", AUTOCOMPLETE_CONFIG.latest_query);
        AUTOCOMPLETE_CONFIG.sent_latest = true;
        const qry = AUTOCOMPLETE_CONFIG.latest_query;

        const query_uri = new URL(window.location.protocol + "//" + window.location.host + window.QUERY_URI + "query");
        query_uri.searchParams.append("q", qry);

        document.getElementById("answer-spinner").style.display = "block";
        const res = await fetch(query_uri, {
            method: "GET",
            mode: "cors",
            cache: "no-cache",

        });
        document.getElementById("answer-spinner").style.display = "none";

        if (AUTOCOMPLETE_CONFIG.suppress_next) {
            // do not render result, user tried to delete something while retrieving results
            return;
        }
        const res_data = await res.json();
        res_data.results.sort(function(obj1, obj2) {
                          return obj2.size - obj1.size;
                        });
        console.log("[query-res]", res_data.results);
        populateResultList(res_data.results);

        if (res_data.results && res_data.results.length === 1) {
            fetchAnswer(res_data.results[0], true);
        }
        if (res_data.results && res_data.results.length > 0) {
            if (res_data.results[0].answerable) {
                fetchAnswer(res_data.results[0], false);
            }
        }

        const ac_input_classes = document.querySelectorAll(".ac_inputgroup")[0].classList
        ac_input_classes.add("ac_suggestions_active");
    }

    function updateQuery(query) {
        console.log("[query-input]", query);
        AUTOCOMPLETE_CONFIG.latest_query = query;
        AUTOCOMPLETE_CONFIG.sent_latest = false;

        if (!AUTOCOMPLETE_CONFIG.interval_handle) {
            AUTOCOMPLETE_CONFIG.interval_handle = setInterval(runQuery, AUTOCOMPLETE_CONFIG.throttle);
            runQuery();
        }
    }

    function changeHandler(evt) {
        const query = evt.target.value;
        updateQuery(query);

        evt.preventDefault();
        return false;
    };
    input.addEventListener("input", changeHandler);
    input.addEventListener("propertychange", changeHandler);

    function getSuggestions() {
        return document.querySelectorAll(".ac_suggestions > .list-group-item");
    }

    function updateActiveSuggestion(dontforce) {
        const previous_cursor = input.selectionStart;
        const manual_length = (AUTOCOMPLETE_CONFIG.latest_query || "").length;
        if (dontforce && manual_length === 0) { return; }

        const active_idx = getActiveSuggestionIndex();
        const suggested_text = getSuggestions()[active_idx];

        if (suggested_text && suggested_text.dataset.suggestion) {
            if (dontforce && input.value && !suggested_text.dataset.suggestion.toLowerCase().startsWith(input.value.toLowerCase())) {
                console.log("[input-update-suppressed] input:", input.value, "suggested:", suggested_text.dataset.suggestion, "prev:", AUTOCOMPLETE_CONFIG.latest_query);
                return;
            }
            console.log("[input-update] input:", input.value, "suggested:", suggested_text.dataset.suggestion, "prev:", AUTOCOMPLETE_CONFIG.latest_query);
            input.value = suggested_text.dataset.suggestion;
            input.setSelectionRange(manual_length, suggested_text.dataset.suggestion.length);
        }
    }

    function getActiveSuggestionIndex() {
        let active_idx = -1;
        const suggestions = getSuggestions();
        for (let idx = 0; idx < suggestions.length; idx++) {
            let suggestion = suggestions[idx];
            if (suggestion.classList.contains("active")) {
                active_idx = idx;
                break;
            }
        }
        if (active_idx === -1) {
            active_idx = 0;
        }
        return active_idx;
    }

    function moveActiveSuggestion(dir) {
        const suggestions = getSuggestions();
        let active_idx = getActiveSuggestionIndex();
        suggestions[active_idx].classList.remove("active");

        active_idx += dir;

        if (active_idx < 0) {
            active_idx = suggestions.length - 1 + active_idx;
        } else if (active_idx >= suggestions.length) {
            active_idx -= suggestions.length;
        }
        suggestions[active_idx].classList.add("active");
        updateActiveSuggestion();
    }

    function keyHandler(evt) {
        if (evt.which === 8) { // backspace
            if (input.selectionStart > 1) {
                input.selectionStart = input.selectionStart - 1;
            }
            AUTOCOMPLETE_CONFIG.suppress_next = true;
            AUTOCOMPLETE_CONFIG.latest_query = input.value;
            AUTOCOMPLETE_CONFIG.sent_latest = false;
        }

       switch(evt.which) {
               case 9: // tab
               input.setSelectionRange(input.value.length, input.value.length);
               if (AUTOCOMPLETE_CONFIG.latest_query !== input.value) {
                   AUTOCOMPLETE_CONFIG.latest_query = input.value;
                   AUTOCOMPLETE_CONFIG.sent_latest = false;
               }
               break;
               case 13: // enter
               input.setSelectionRange(input.value.length, input.value.length);
               if (AUTOCOMPLETE_CONFIG.latest_query !== input.value) {
                  AUTOCOMPLETE_CONFIG.latest_query = input.value;
                  AUTOCOMPLETE_CONFIG.sent_latest = false;
              }
               break;
               case 38: // up
               moveActiveSuggestion(-1);
               break;
               case 40: // down
               moveActiveSuggestion(1);
               break;
               default: return;
           }
           evt.preventDefault();
           return false;
    }
    input.addEventListener("keydown", keyHandler);

    console.log("[autocomplete] initialized");
}

function setupSearchField() {
    const searchButton = document.getElementById('query-button');
    const searchInput = document.getElementById('q');
    searchButton.addEventListener('click', () => {
      const inputValue = searchInput.value;
      alert(inputValue);
    });

    setupAutoComplete(searchInput);
}

(function() {
    console.log("[init]");
    setupSearchField();
    document.getElementById("answer-spinner").style.display = "none";
    document.getElementById("sparql-toggle").style.display = "none";
})();