Light = function(id, hostname) {
    this.hostname = hostname;
    this.id = id;
    this.state = false;
    this.obj = this.gen();
};
Light.prototype.gen = function() {
    return $("<div />", {text: 'Light: ' + this.hostname})
        .click($.proxy(this.toggle, this));
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
    this.obj.css('color', state ? 'red' : 'blue');
};
Light.prototype.render = function() {
    $('body').append(this.obj);
};
