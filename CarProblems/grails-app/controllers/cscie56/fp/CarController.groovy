package cscie56.fp

import grails.plugin.springsecurity.annotation.Secured

import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional

@Transactional(readOnly = true)

class CarController {

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def userService
    def carService

    @Secured(['ROLE_ADMIN', 'ROLE_USER', 'ROLE_ANONYMOUS'])
    def welcomePage() {
        Boolean isAdminLoggedin = userService.isAdminLoggedin()
        respond Car.list(), model: [isAdminLoggedin:isAdminLoggedin]
    }

    @Secured(['ROLE_ADMIN', 'ROLE_USER', 'ROLE_ANONYMOUS'])
    def example() {
        Car carInstance = Car.get(1)
        List carProblemsSummaryReport = carService.generateCarProblemsSummaryReport(carInstance)
        List allCarProblems = Problem.findAllByCarAndApproved(carInstance, true, [sort:"dateSubmitted", order: "desc"])
        respond Car.get(1), view: 'carProfile', model: [carProblemsSummaryReport:carProblemsSummaryReport, allCarProblems:allCarProblems]
    }

    @Secured(['ROLE_ADMIN', 'ROLE_USER', 'ROLE_ANONYMOUS'])
    def carProfile (Car carInstance) {

        List carProblemsSummaryReport = carService.generateCarProblemsSummaryReport(carInstance)
        List allCarProblems = Problem.findAllByCarAndApproved(carInstance, true, [sort:"dateSubmitted", order: "desc"])
        Boolean isAdminLoggedin = userService.isAdminLoggedin()
        respond carInstance, model: [isAdminLoggedin:isAdminLoggedin, carProblemsSummaryReport:carProblemsSummaryReport, allCarProblems:allCarProblems]

    }

    @Secured(['ROLE_ADMIN'])
    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond Car.list(params), model:[carInstanceCount: Car.count()]
    }

    @Secured(['ROLE_ADMIN'])
    def show(Car carInstance) {
        respond carInstance
    }

    @Secured(['ROLE_ADMIN'])
    def create() {
        respond new Car(params)
    }

    @Transactional
    @Secured(['ROLE_ADMIN'])
    def save(Car carInstance) {
        if (carInstance == null) {
            notFound()
            return
        }

        if (carInstance.hasErrors()) {
            respond carInstance.errors, view:'create'
            return
        }

        carInstance.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'car.label', default: 'Car'), carInstance.id])
                redirect carInstance
            }
            '*' { respond carInstance, [status: CREATED] }
        }
    }

    @Secured(['ROLE_ADMIN'])
    def edit(Car carInstance) {
        respond carInstance
    }

    @Transactional
    def update(Car carInstance) {
        if (carInstance == null) {
            notFound()
            return
        }

        if (carInstance.hasErrors()) {
            respond carInstance.errors, view:'edit'
            return
        }

        carInstance.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'Car.label', default: 'Car'), carInstance.id])
                redirect carInstance
            }
            '*'{ respond carInstance, [status: OK] }
        }
    }

    @Transactional
    @Secured(['ROLE_ADMIN'])
    def delete(Car carInstance) {

        if (carInstance == null) {
            notFound()
            return
        }

        carInstance.delete flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'Car.label', default: 'Car'), carInstance.id])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'car.label', default: 'Car'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}
