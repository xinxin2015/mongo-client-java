Object.extend = function( dst , src , deep ){
    for ( var k in src ){
        var v = src[k];
        if ( deep && typeof(v) == "object" ){
            if ( "floatApprox" in v ) { // convert NumberLong properly
                eval( "v = " + tojson( v ) );
            } else {
                v = Object.extend( typeof ( v.length ) == "number" ? [] : {} , v , true );
            }
        }
        dst[k] = v;
    }
    return dst;
}