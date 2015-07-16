

var SearchPage = React.createClass({displayName: "SearchPage",
  notify: function(msg) {

  },
  handleKeywordQuery: function(keywords) {
    facets = []
    $.each(this.state.extractors, function(index, value) {
      if (value.active)
        facets.push(value.name); });
    $.ajax({
      url: 'docs?keywords=' + encodeURIComponent(keywords) + 
            '&facets=' + facets.join(),
      success: function(data) {
        this.setState({data: data, keywords:keywords});
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },
  handleShowMore: function() {
    var start = this.state.data.hits.length
    facets = []
    $.each(this.state.extractors, function(index, value) {
      if (value.active)
        facets.push(value.name); });
    $.ajax({
      url: 'docs?start=' + start + '&keywords=' + encodeURIComponent(this.state.keywords) + 
            '&facets=' + facets.join(),
      success: function(data) {
        var all = { 'total': data.total, 'hits': this.state.data.hits.concat(data.hits) }
        this.setState({data: all, keywords:this.state.keywords});
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });    
  },
  handleFacetChange: function(name, active) {
    $.each(this.state.extractors, function(index, value) {
      if (value.name == name)
        value.active = active; });
    this.handleKeywordQuery(this.state.keywords);
  },
  handleLoadExtractors: function() {
    $.ajax({
      url: 'annotators',
      success: function(data) {
        // add a field to represent active/non-active
        extractors = data.map(function(it) {
          return {
            'name': it._source.name,
            'active': false
          };
        })
        if (this.isMounted()) {
          this.setState({extractors:extractors}); 
        }
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },
  getInitialState: function() {
    return {data: {hits:[]}, extractors: [], keywords: ''};
  },
  componentDidMount: function() {
    this.handleLoadExtractors()
    this.handleKeywordQuery('')
  },
  render: function() {
    return (
      React.createElement("div", null, 
        React.createElement(Header, {onKeywordQuery: this.handleKeywordQuery}), 
        React.createElement(NotificationBox, null), 
        React.createElement(Content, {style: {'height':'100%'}, 
          data: this.state.data, 
          extractors: this.state.extractors, 
          onFacetChange: this.handleFacetChange, 
          onShowMore: this.handleShowMore})
      )
    );
  }
});

var Header = React.createClass({displayName: "Header",
  setFocus: function() {
    React.findDOMNode(this.refs.query).focus();
  },
  componentDidMount: function() {
    this.setFocus();
  },
  getInitialState: function() {
    return {query: ''};
  },
  inputSubmit: function() {
    var val = this.state.query;
    this.props.onKeywordQuery(val)
  },
  handleChange: function(evt) {
    this.setState({query: evt.target.value});
  },
  handleKeyDown: function(evt) {
      if (evt.keyCode == 13 ) {
          return this.inputSubmit();
      }
  },
  render: function() {
    return (
      React.createElement("div", {className: "header unselectable"}, 
        React.createElement("div", {style: {position:'absolute', top:0, left:0, marginLeft:'10px', marginTop:'10px', cursor:'pointer'}}, 
          React.createElement("span", {style: {fontFamily:'Open Sans, sans-serif', fontSize:'22px'}})
        ), 
        React.createElement("div", {style: {position:'absolute', top:0, left:200}}, 
          React.createElement("input", {type: "text", ref: "query", value: this.state.query, 
            onChange: this.handleChange, onKeyDown: this.handleKeyDown})
        )
      )
    );
  }
});

var NotificationBox = React.createClass({displayName: "NotificationBox",
  render: function() {
    return (React.createElement("div", null));
  }
})

var Content = React.createClass({displayName: "Content",
  render: function() {
    return (
      React.createElement("div", {className: "content"}, 
        React.createElement(LeftMenu, {extractors: this.props.extractors, 
          onFacetChange: this.props.onFacetChange}), 
        React.createElement(Results, {data: this.props.data, onShowMore: this.props.onShowMore}), 
        React.createElement(Help, null)
      )
      );
  }
})

var LeftMenu = React.createClass({displayName: "LeftMenu",
  render: function() {
    var onFacetChange = this.props.onFacetChange;
    var extractorNodes = this.props.extractors.map(function(ex) {
      return (
        React.createElement(Facet, {data: ex, onFacetChange: onFacetChange})
        );
    });
    return (React.createElement("div", {className: "leftmenu"}, React.createElement("br", null), 
      extractorNodes
      ));
  }
})

var Facet = React.createClass({displayName: "Facet",
  handleClick: function() {
    var active = !this.props.data.active;
    this.props.onFacetChange(this.props.data.name, active);
  },
  render: function() {
    var classes = 'facet';
    if (!this.props.data.active)
      classes += ' facet-inactive';
    return (React.createElement("div", {className: classes, onClick: this.handleClick}, 
       React.createElement("div", {style: {display:'inline-block',width:'30px'}}, 
         React.createElement("i", {className: "fa fa-check"})
       ), 
       this.props.data.name))
  }
})

var Help = React.createClass({displayName: "Help",
  render: function() {
    return (React.createElement("div", {className: "help"}));
  }
})

var Results = React.createClass({displayName: "Results",
  handleShowMoreClick: function() {
    this.props.onShowMore();
  },

  render: function() {
    var resultNodes = this.props.data.hits.map(function(result) {
       return (
         React.createElement(Result, {data: result})
         );
     });
    var showMoreButton = ''
    if (this.props.data.hits.length > 0 && this.props.data.hits.length < this.props.data.total) {
       showMoreButton = (React.createElement("div", {style: {cursor:'pointer'}, onClick: this.handleShowMoreClick}, "Show more"))
     }

     return (React.createElement("div", {style: {marginLeft:'200px', marginRight:'200px'}}, 
       React.createElement("div", {style: {'textAlign':'right', paddingTop:'10px', paddingBottom:'5px', 'color':'#AAA'}}, 
         this.props.data.total, " results"
       ), 
       resultNodes, 
       showMoreButton
       ));
  }
})

var Result = React.createClass({displayName: "Result",
  getInitialState: function() {
   return {layers: [
     { name: "Extractors", active: true },
     { name: "Tokens", active: false },
     { name: "Sentences", active: false },
     { name: "Dependencies", active: false },
     { name: "Lemmas", active: false },
     { name: "PartOfSpeech", active: false },
     { name: "Details", active: false },
     ]};
  },
  onLayerChange: function(name, active) {
    $.each(this.state.layers, function(index, value) {
      if (value.name == name)
        value.active = active; 
    })
    if (this.isMounted()) {
      this.setState({layers:this.state.layers}); 
    }
  },
  render: function() {
    return (React.createElement("div", {className: "result"}, 
         React.createElement(TextWithAnnotations, {data: this.props.data, layers: this.state.layers}), 
         React.createElement(AnnotationsSelector, {layers: this.state.layers, onLayerChange: this.onLayerChange})
      ));
  }  
})



React.render(
  React.createElement(SearchPage, null),
  document.getElementById('page')
);
