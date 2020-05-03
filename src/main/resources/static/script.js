const tempColors = ["#ff3800", "#ff5300", "#ff6500", "#ff7300", "#ff7e00", "#ff8912", "#ff932c", "#ff9d3f", "#ffa54f", "#ffad5e", "#ffb46b", "#ffbb78", "#ffc184", "#ffc78f", "#ffcc99", "#ffd1a3", "#ffd5ad", "#ffd9b6", "#ffddbe", "#ffe1c6", "#ffe4ce", "#ffe8d5", "#ffebdc", "#fff5f5", "#f5f3ff", "#e6ebff", "#dae4ff", "#d1dfff", "#cadaff", "#c4d7ff", "#c0d4ff", "#bcd1ff", "#b8cfff", "#b3ccff", "#afc9ff", "#abc7ff", "#a9c5ff", "#a6c3ff", "#a6c3ff"];

Control = function() {
    this.container = $('#control');
    this.brightness = this.container.find('#brightness');
    this.hue = this.container.find('#hue');
    this.temp = this.container.find('#temp');
    this.name = this.container.find('#name');
    this.host = this.container.find('#host');
    this.icon = this.container.find('.icon');
    this.close = this.container.find('.close');
    this.mode = 0;

    this.close.click(() => this.container.hide());
    this.container.click(() => this.container.hide());
    this.container.children('div').click((e) => e.stopPropagation());

    this.hue.on('change', () => this.mode = 0);
    this.hue.on('change mousemove', () => this.updateIcon());
    this.brightness.on('change mousemove', () => this.updateIcon());
    this.temp.on('change', () => this.mode = 1);
    this.temp.on('change mousemove', () => this.updateIcon());

    this.icon.click(() => {
        if (+this.brightness.val() > 0) {
            this.brightness.val(0);
        } else {
            this.brightness.val(100);
        }
        this.updateIcon();
    });
};
Control.prototype.updateIcon = function() {
    let color;
    const oldState = Object.assign({}, this.light.state);
    oldState.state = +this.brightness.val();
    oldState.temp = null;
    oldState.hue = null;

    if (oldState.state === 0) {
        color = '#000';
    } else if (this.mode === 0) {
        const brightness = (+this.brightness.val() + 30) * 0.6;
        oldState.hue = +this.hue.val();

        color = `hsl(${oldState.hue},100%,${brightness}%)`;
    } else {
        oldState.temp = this.temp.val() * 50;
        color = tempColors[Math.floor(this.temp.val() / 2) - 27];
    }
    this.icon.css({'color': color});

    this.light.setState(oldState);
};
Control.prototype.show = function(light) {
    this.mode = light.state.temp == null && light.state.state > 0 ? 0 : 1;

    this.container.show();
    this.name.val(light.name);
    this.host.val(light.hostname);
    this.brightness.val(light.state.state);
    this.hue.val(light.state.hue);
    this.temp.val(light.state.state > 0 ? light.state.temp / 50 : 64);
    this.light = light;

    this.updateIcon();
};
Light = function(id, name, hostname) {
    this.hostname = hostname;
    this.name = name;
    this.id = id;
    this.state = {state: 0};
    this.obj = this.gen();

    this.lastUpdate = Date.now();
    this.updateQueued = false;
    this.updateRunning = false;
    this.nextState = JSON.stringify(this.state);

    this.fetchState();
};
Light.prototype.gen = function() {
    return $("<div />", {class: 'far'})
        .click($.proxy(this.toggle, this))
        .append($("<span />", {html: '&#xf0eb;'}))
        .append(this.name);
};
Light.prototype.toggle = function() {
    /*const that = this;
    const newState = !this.state;

    $.getJSON("/control/" + this.id + "/" + (newState ? "on" : "off"), function(data) {
        if (data.success) {
            that.updateState(newState);
        }
    });*/
    window.control.show(this);
};
Light.prototype.fetchState = function() {
    const that = this;

    $.getJSON("/control/" + this.id, function(data) {
        that.updateState(data);
    });
};
Light.prototype.setState = function(state) {
    const stateJSON = JSON.stringify(state);
    if (stateJSON === this.nextState) return;
    this.nextState = stateJSON;

    if (Date.now() - this.lastUpdate < 500) {
        this.updateQueued = true;

        if (!this.updateRunning) {
            this.updateRunning = true;
            setTimeout($.proxy(this.runUpdate, this), 500);
        }
    } else if (!this.updateRunning) {
        this.runUpdate();
    }
};
Light.prototype.runUpdate = function() {
    const that = this;
    this.updateQueued = false;
    this.updateRunning = true;
    this.lastUpdate = Date.now();

    $.ajax({url: "/control/" + this.id, type: 'PUT', data: this.nextState, dataType: 'json', contentType: 'application/json'}).done(
        function(data) {
            that.updateState(data);
        }
    ).always(
        function() {
            if (that.updateQueued) {
                setTimeout($.proxy(that.runUpdate, that), 500);
            } else {
                that.updateRunning = false;
            }
        }
    );
};
Light.prototype.updateState = function(state) {
    this.state = state;
    this.obj.css('color', (state.state > 0) ? '#5d5' : '#888');
};
Light.prototype.render = function() {
    $('body').append(this.obj);
};
$(function () {
    window.control = new Control();
});