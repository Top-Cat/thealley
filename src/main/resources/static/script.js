Light = function(id, name, hostname) {
    this.hostname = hostname;
    this.name = name;
    this.id = id;
    this.state = false;
    this.obj = this.gen();
};
Light.prototype.gen = function() {
    return $("<div />", {class: 'far'})
        .click($.proxy(this.toggle, this))
        .append($("<span />", {html: '&#xf0eb;'}))
        .append(this.name);
};
Light.prototype.toggle = function() {
    that = this;
    newState = !this.state;

    $.getJSON("/control/" + (newState ? "on" : "off") + "/" + this.id, function(data, status, xhr) {
        if (data.success) {
            that.updateState(newState);
        }
    });
};
Light.prototype.updateState = function(state) {
    this.state = state;
    this.obj.css('color', state ? '#5d5' : '#888');
};
Light.prototype.render = function() {
    $('body').append(this.obj);
};
