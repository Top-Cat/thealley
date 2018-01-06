Light = function(id, name, hostname) {
    this.hostname = hostname;
    this.name = name;
    this.id = id;
    this.state = false;
    this.obj = this.gen();
    this.fetchState();
};
Light.prototype.gen = function() {
    return $("<div />", {class: 'far'})
        .click($.proxy(this.toggle, this))
        .append($("<span />", {html: '&#xf0eb;'}))
        .append(this.name);
};
Light.prototype.toggle = function() {
    var that = this;
    var newState = !this.state;

    $.getJSON("/control/" + this.id + "/" + (newState ? "on" : "off"), function(data, status, xhr) {
        if (data.success) {
            that.updateState(newState);
        }
    });
};
Light.prototype.fetchState = function() {
    var that = this;

    $.getJSON("/control/" + this.id, function(data, status, xhr) {
        that.updateState(data.state);
    });
};
Light.prototype.updateState = function(state) {
    this.state = state;
    this.obj.css('color', state ? '#5d5' : '#888');
};
Light.prototype.render = function() {
    $('body').append(this.obj);
};
